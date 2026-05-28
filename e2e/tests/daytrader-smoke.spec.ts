import { expect, test } from '@playwright/test';

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

  test('configuration and primitive surfaces remain directly reachable', async ({ page }) => {
    await page.goto('config');

    await expect(page).toHaveTitle(/Welcome to DayTrader/i);
    await expect(page.getByRole('button', { name: /Update Config/i })).toBeVisible();

    await page.goto('servlet/PingServlet');

    await expect(page.locator('body')).toContainText('Ping Servlet');
  });
});