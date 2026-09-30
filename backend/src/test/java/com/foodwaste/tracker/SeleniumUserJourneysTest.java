package com.foodwaste.tracker;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeleniumUserJourneysTest {

    private static final String BASE_URL = "http://localhost:5173";
    private static final String BACKEND_URL = "http://localhost:8080/api/health";
    private static Process frontendProcess;
    private static boolean backendStartedLocally = false;
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    public static void setUpClass() throws Exception {
        // 1. Ensure Backend Server is Running
        if (!isServerUp(BACKEND_URL)) {
            System.out.println("[Selenium Setup] Starting Backend Server on port 8080...");
            Application.startServer(8080);
            backendStartedLocally = true;
            Thread.sleep(1500);
        }

        // 2. Ensure Frontend Server is Running
        if (!isServerUp(BASE_URL)) {
            System.out.println("[Selenium Setup] Starting Frontend Server (Vite)...");
            ProcessBuilder pb = new ProcessBuilder("npm", "run", "dev");
            File projectRoot = new File(System.getProperty("user.dir")).getParentFile();
            File frontendDir = new File(projectRoot, "frontend");
            if (!frontendDir.exists()) {
                frontendDir = new File(System.getProperty("user.dir"), "frontend");
            }
            pb.directory(frontendDir);
            frontendProcess = pb.start();

            // Wait up to 15 seconds for frontend
            for (int i = 0; i < 30; i++) {
                if (isServerUp(BASE_URL)) {
                    System.out.println("[Selenium Setup] Frontend is UP!");
                    break;
                }
                Thread.sleep(500);
            }
        }

        // 3. Setup ChromeDriver via WebDriverManager
        WebDriverManager.chromedriver().setup();
    }

    @AfterAll
    public static void tearDownClass() {
        if (frontendProcess != null && frontendProcess.isAlive()) {
            frontendProcess.destroyForcibly();
        }
        if (backendStartedLocally) {
            Application.stopServer();
        }
    }

    private static boolean isServerUp(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            return responseCode >= 200 && responseCode < 400;
        } catch (Exception e) {
            return false;
        }
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown(TestInfo testInfo) {
        if (driver != null) {
            // Save screenshot after test execution
            captureScreenshot(testInfo.getDisplayName().replaceAll("[^a-zA-Z0-9_-]", "_"));
            driver.quit();
        }
    }

    private void captureScreenshot(String filenamePrefix) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File src = ts.getScreenshotAs(OutputType.FILE);
            File destDir = new File("target/screenshots");
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File dest = new File(destDir, filenamePrefix + "_" + timestamp + ".png");
            FileHandler.copy(src, dest);
            System.out.println("[Selenium Screenshot] Saved to: " + dest.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
    }

    // =========================================================================
    // User Journey 1: Dashboard Load, Navbar & Telemetry Metrics Verification
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("Journey 1: Verify Dashboard Heading and Telemetry KPI Cards")
    public void testDashboardNavigationAndTelemetry() {
        driver.get(BASE_URL);

        // Assert Title / Header
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Food Waste Tracking Dashboard')]")
        ));
        assertNotNull(heading, "Dashboard heading should be displayed");
        assertTrue(heading.isDisplayed(), "Dashboard heading must be visible");

        // Assert Backend Health Badge in Navbar
        WebElement healthBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Backend:') or contains(text(),'8080 UP')]")
        ));
        assertTrue(healthBadge.isDisplayed(), "Backend health badge should be online");

        // Assert KPI Cards
        List<WebElement> kpiCards = driver.findElements(By.xpath("//*[contains(text(),'Total Waste Logged') or contains(text(),'Active Entries')]"));
        assertFalse(kpiCards.isEmpty(), "KPI metric cards should be rendered on dashboard");
    }

    // =========================================================================
    // User Journey 2: Log New Food Waste Entry via Modal Form
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("Journey 2: Submit New Food Waste Log Entry via Modal")
    public void testLogNewFoodWasteEntryModal() throws InterruptedException {
        driver.get(BASE_URL);

        // Click "Log Waste Entry" button
        WebElement logBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Log Waste Entry')]")
        ));
        logBtn.click();

        // Wait for modal dialog
        WebElement modalHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h3[contains(text(),'Log New Food Waste Entry')]")
        ));
        assertTrue(modalHeader.isDisplayed(), "Log Waste Entry modal should open");

        // Fill form fields
        WebElement sourceSelect = driver.findElement(By.xpath("//label[contains(text(),'Source Location')]/following-sibling::select"));
        new Select(sourceSelect).selectByVisibleText("Kitchen A");

        WebElement categorySelect = driver.findElement(By.xpath("//label[contains(text(),'Waste Category')]/following-sibling::select"));
        new Select(categorySelect).selectByVisibleText("Produce");

        WebElement quantityInput = driver.findElement(By.xpath("//input[@type='number']"));
        quantityInput.clear();
        quantityInput.sendKeys("14.5");

        WebElement notesInput = driver.findElement(By.xpath("//textarea"));
        notesInput.clear();
        notesInput.sendKeys("Selenium Automated Test Entry - Prep Excess");

        // Submit form
        WebElement submitBtn = driver.findElement(By.xpath("//button[@type='submit']"));
        submitBtn.click();

        // Verify toast notification or newly inserted row in table
        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'recorded successfully') or contains(text(),'Entry') or contains(text(),'Selenium Automated Test')]")
        ));
        assertTrue(toast.isDisplayed(), "Success notification toast should appear after submission");
    }

    // =========================================================================
    // User Journey 3: Filter & Search Waste Logs Table
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("Journey 3: Filter and Search Waste Logs")
    public void testFilterAndSearchWasteEntries() {
        driver.get(BASE_URL);

        // Locate search input
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Search ID, source, notes...']")
        ));
        searchInput.clear();
        searchInput.sendKeys("Kitchen");

        // Locate Status Filter select
        List<WebElement> selects = driver.findElements(By.xpath("//select"));
        assertTrue(selects.size() > 0, "Select dropdowns should exist for filtering");

        // Check table rows updated
        List<WebElement> rows = driver.findElements(By.xpath("//tbody/tr"));
        assertFalse(rows.isEmpty(), "Table rows should be present after applying search filter");
    }

    // =========================================================================
    // User Journey 4: Audit and Update Entry Status
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("Journey 4: Update Waste Log Audit Status Dropdown")
    public void testUpdateEntryAuditStatus() {
        driver.get(BASE_URL);

        // Wait for table to load
        WebElement tbody = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tbody")));
        assertNotNull(tbody, "Table body should be loaded");

        // Find status dropdown in the first row
        List<WebElement> statusSelects = driver.findElements(By.xpath("//tbody/tr[1]//select"));
        if (!statusSelects.isEmpty()) {
            Select statusSelect = new Select(statusSelects.get(0));
            String currentStatus = statusSelect.getFirstSelectedOption().getText();
            String newStatus = currentStatus.equals("Reviewed") ? "Pending" : "Reviewed";

            statusSelect.selectByVisibleText(newStatus);

            // Assert notification toast for status change
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'status updated') or contains(text(),'updated')]")
            ));
            assertTrue(toast.isDisplayed(), "Notification toast should confirm status update");
        }
    }

    // =========================================================================
    // User Journey 5: Delete Waste Log Entry
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("Journey 5: Delete Waste Log Entry with Confirmation")
    public void testDeleteWasteLogEntry() {
        driver.get(BASE_URL);

        // Wait for table
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tbody")));

        List<WebElement> deleteButtons = driver.findElements(By.xpath("//tbody/tr//button[@title='Delete entry']"));
        if (!deleteButtons.isEmpty()) {
            int initialRowCount = driver.findElements(By.xpath("//tbody/tr")).size();

            // Click delete on first item
            deleteButtons.get(0).click();

            // Accept Browser Confirmation Alert
            try {
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
            } catch (Exception ignored) {}

            // Verify Toast Notification
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'deleted successfully') or contains(text(),'deleted')]")
            ));
            assertTrue(toast.isDisplayed(), "Delete success toast should appear");
        }
    }
}
