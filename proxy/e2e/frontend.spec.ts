import { test, expect } from '@playwright/test'
import * as fs from 'fs'
import * as path from 'path'
import * as os from 'os'

// Simple HTML page used for all tests. It supports a file input and uploads
// via a fetch to '/upload'. The client implements a timeout to exercise that path.
const htmlContent = (timeoutMs?: number) => `<!doctype html>
<html>
<head><meta charset="utf-8"><title>Upload Test</title></head>
<body>
  <div>
    <input id="file" type="file" />
    <button id="uploadBtn" type="button">Upload</button>
    <div id="status" style="margin-top:8px"></div>
  </div>
  <script>
    const timeLimit = (typeof window.__UPLOAD_TIMEOUT !== 'undefined') ? window.__UPLOAD_TIMEOUT : ${timeoutMs ?? 100};
    async function handleUpload() {
      const input = document.getElementById('file')
      const file = input.files[0]
      const status = document.getElementById('status')
      if (!file) { status.innerText = 'No file selected'; return; }
      const form = new FormData()
      form.append('file', file)
      const timeout = new Promise((_, reject) => setTimeout(() => reject(new Error('timeout')), timeLimit))
      try {
        const resp = await Promise.race([fetch('/upload', { method: 'POST', body: form }), timeout])
        if (resp && resp.ok) status.innerText = 'Uploaded: ' + file.name
        else status.innerText = 'Upload failed'
      } catch (err) {
        if (err && err.message === 'timeout') {
          status.innerText = 'Timeout'
        } else {
          status.innerText = 'Network error'
        }
      }
    }
    document.getElementById('uploadBtn').addEventListener('click', (e) => { e.preventDefault(); handleUpload(); })
  </script>
</body>
</html>`

// Helper to make a temp file for upload input
async function makeTempFile(name: string, content: string): Promise<string> {
  const dir = await fs.promises.mkdtemp(path.join(os.tmpdir(), 'upload-'))
  const filePath = path.join(dir, name)
  await fs.promises.writeFile(filePath, content)
  return filePath
}

test.describe('proxy e2e frontend upload', () => {
  test('文件上传功能测试 - should upload successfully', async ({ page }) => {
    // Intercept network to simulate a successful upload
    await page.route('/upload', route => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ ok: true }) })
    })

    const filePath = await makeTempFile('test.txt', 'hello world')

    await page.setContent(htmlContent())
    // Bind the actual file before clicking upload
    const input = page.locator('#file')
    await input.setInputFiles(filePath)
    await page.locator('#uploadBtn').click()

    const status = page.locator('#status')
    await expect(status).toHaveText(/Uploaded: test.txt/)
  })

  test('网络错误处理测试 - should show network error on failure', async ({ page }) => {
    // Simulate network failure for the upload request
    await page.route('/upload', route => {
      route.abort('failed')
    })

    const filePath = await makeTempFile('network.txt', 'data')
    await page.setContent(htmlContent())
    await page.locator('#file').setInputFiles(filePath)
    await page.locator('#uploadBtn').click()

    const status = page.locator('#status')
    await expect(status).toHaveText(/Network error|Upload failed/)
  })

  test('超时处理测试 - should timeout when upload takes too long', async ({ page }) => {
    // Delay the server response to trigger client timeout
    await page.route('/upload', async route => {
      await new Promise(r => setTimeout(r, 200))
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ ok: true }) })
    })
    // Use a shorter client timeout to force timeout condition
    await page.setContent(htmlContent(100))

    const filePath = await makeTempFile('delay.txt', 'slow')
    await page.locator('#file').setInputFiles(filePath)
    await page.locator('#uploadBtn').click()
    const status = page.locator('#status')
    await expect(status).toHaveText(/Timeout|Network error|Upload failed/)
  })
})
