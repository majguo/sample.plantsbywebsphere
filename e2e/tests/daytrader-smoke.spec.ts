import { expect, test } from '@playwright/test';

function uniqueUserId(): string {
  return `uid:pw-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
}

test.describe('DayTrader browser parity harness', () => {
  test('login home and logout stay on the preserved JSP surface', async ({ page }) => {
    await page.goto('welcome.jsp');

    await expect(page).toHaveTitle(/DayTrader Login/i);
    await expect(page.getByRole('link', { name: /Register With DayTrader/i })).toBeVisible();

    await page.getByRole('button', { name: /Log in/i }).click();

    await expect(page).toHaveTitle(/Welcome to DayTrader/i);
    await expect(page.getByText('Welcome uid:0,')).toBeVisible();
    await expect(page.getByRole('link', { name: 'Quotes/Trade' })).toBeVisible();

    await page.getByRole('link', { name: /Logoff/i }).click();

    await expect(page).toHaveTitle(/DayTrader Login/i);
    await expect(page.getByRole('button', { name: /Log in/i })).toBeVisible();
  });

  test('registration, buy, portfolio sell, and account validation stay functional in the browser', async ({ page }) => {
    const userId = uniqueUserId();
    const emailAddress = `${userId.replace(/[^a-zA-Z0-9]/g, '-') }@example.com`;

    await page.goto('register.jsp');

    await expect(page).toHaveTitle(/DayTrader Registration/i);
    await page.locator('input[name="Full Name"]').fill('Playwright Trader');
    await page.locator('input[name="snail mail"]').fill('1 Test Way');
    await page.locator('input[name="email"]').fill(emailAddress);
    await page.locator('input[name="user id"]').fill(userId);
    await page.locator('input[name="passwd"]').fill('secret');
    await page.locator('input[name="confirm passwd"]').fill('different');
    await page.getByRole('button', { name: /Submit Registration/i }).click();

    await expect(page).toHaveTitle(/DayTrader Registration/i);
    await expect(page.locator('body')).toContainText('passwords did not match');

    await page.locator('input[name="passwd"]').fill('secret');
    await page.locator('input[name="confirm passwd"]').fill('secret');
    await page.getByRole('button', { name: /Submit Registration/i }).click();

    await expect(page).toHaveTitle(/Welcome to DayTrader/i);
    await expect(page.locator('body')).toContainText(`Welcome ${userId},`);

    await page.getByRole('link', { name: 'Quotes/Trade' }).click();
    await expect(page).toHaveTitle(/DayTrader: Quotes and Trading/i);
    await page.locator('input[name="quantity"]').first().fill('1');
    await page.locator('input[type="submit"][value="buy"]').first().click();

    await expect(page).toHaveTitle(/DayTrader Order information/i);
    await expect(page.locator('body')).toContainText('has been submitted for processing');

    await page.getByRole('link', { name: 'Portfolio' }).click();
    await expect(page).toHaveTitle(/DayTrader Portfolio/i);
    await expect(page.locator('body')).toContainText('Number of Holdings:');
    await expect(page.locator('body')).toContainText('sell');
    await page.locator('a[href*="action=sell"]').first().click();

    await expect(page).toHaveTitle(/DayTrader Order information/i);
    await expect(page.locator('body')).toContainText('Order');
    await expect(page.locator('body')).toContainText('sell');

    await page.getByRole('link', { name: 'Account' }).click();
    await expect(page).toHaveTitle(/DayTrader Account Information/i);
    await page.locator('input[name="password"]').fill('secret');
    await page.locator('input[name="cpassword"]').fill('different');
    await page.getByRole('button', { name: /update_profile/i }).click();

    await expect(page).toHaveTitle(/DayTrader Account Information/i);
    await expect(page.locator('body')).toContainText('Update profile error: passwords do not match');

    await page.getByRole('link', { name: /Logoff/i }).click();
    await expect(page).toHaveTitle(/DayTrader Login/i);
  });

  test('configuration updates and primitive surfaces remain directly reachable', async ({ page }) => {
    await page.goto('welcome.jsp');

    await page.getByRole('button', { name: /Log in/i }).click();
    await expect(page).toHaveTitle(/Welcome to DayTrader/i);

    await page.goto('config');

    await expect(page).toHaveTitle(/Welcome to DayTrader/i);
    await expect(page.getByRole('button', { name: /Update Config/i })).toBeVisible();

    const intervalInput = page.locator('input[name="marketSummaryInterval"]');
    const originalInterval = await intervalInput.inputValue();

    await intervalInput.fill('9');
    await page.getByRole('button', { name: /Update Config/i }).click();

    await expect(page.locator('body')).toContainText('DayTrader Configuration Updated');
    await expect(page.locator('input[name="marketSummaryInterval"]')).toHaveValue('9');

    await page.locator('input[name="marketSummaryInterval"]').fill(originalInterval);
    await page.getByRole('button', { name: /Update Config/i }).click();
    await expect(page.locator('input[name="marketSummaryInterval"]')).toHaveValue(originalInterval);

    await page.goto('servlet/PingServlet');

    await expect(page.locator('body')).toContainText('Ping Servlet');
  });
});