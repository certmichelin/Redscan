const puppeteer = require('puppeteer');

if (process.argv.length < 4 || process.argv[2] === '-h' || process.argv[2] === '--help') {
    console.error('Usage: screenshot_puppeteer.js http[s]://ip_or_domain:port outfile.png');
} else {
    (async () => {
        // Running Pupeeteer with an url in format https://IP:PORT will return
        // ERR_CERT_COMMON_NAME_INVALID, even if the certificate is valid for
        // the corresponding domain.
        // => ignore HTTPS errors
        const browser = await puppeteer.launch({ args: ['--no-sandbox', '--disable-setuid-sandbox'], ignoreHTTPSErrors: true });
        const page = await browser.newPage();
        await page.setViewport({
            width: 1280,
            height: 720,
            deviceScaleFactor: 1
        });
        await page.goto(process.argv[2]);
        await page.screenshot({ path: process.argv[3] });

        await browser.close();
    })();
}
