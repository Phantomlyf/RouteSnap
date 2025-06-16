const {app, BrowserWindow, ipcMain, dialog} = require('electron')
const axios = require('axios')
const path = require('path')
const fs = require('fs')
const FormData = require('form-data')

async function postImg(_, filePath) {
    if (!filePath) return { error: "No file selected" }
    
    try {
        const form = new FormData()
        form.append('file', fs.createReadStream(filePath))
      
        const response = await axios.post('http://localhost:4399/upload/image', form, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
        });
      
        return response.data
    } catch (error) {
        console.error('Upload failed:', error)
        return { error: error.message }
    }
}

// 文件选择对话框
function openFile() {
    return dialog.showOpenDialog({
        filters: [{ name: 'Images', extensions: ['jpg', 'png', 'heic'] }],
        properties: ['openFile']
    }).then(result => {
        return result.canceled ? null : result.filePaths[0];
    }).catch(console.error);
}

async function saveWork(_, filePath, detailedPos, context) {
    if (!filePath) return {error: "No file selected"}
    try {
        const form = new FormData()
        form.append('file', fs.createReadStream(filePath))
        form.append('location', detailedPos)
        form.append('content', context)

        const response = await axios.post('http://localhost:4399/upload/travel', form, {
            headers: {
                ...form.getHeaders()
            }
        })

        return response.data
    } catch(e) {
        console.error(e)
        return {error: e.message}
    }
}

async function getAllId(_) {
    try {
        const response = await axios.get('')
        return response.data
    } catch(e) {
        console.error(e)
        return {error: e.message}
    }
}

async function getMsgById(_, id) {
    try {
        const response = await axios.get('', id)
        return response.data
    } catch(e) {
        console.error(e)
        return {error: e.message}
    }
}

app.on('ready', () => {
    const win = new BrowserWindow({
        width: 800,
        height: 600,
        autoHideMenuBar: true,
        webPreferences:{
            preload:path.resolve(__dirname, './preload.js')
        }
    })
    ipcMain.handle('file-open', openFile)
    ipcMain.handle('post-img', postImg)
    ipcMain.handle('save-work', saveWork)
    ipcMain.handle('get-all-id', getAllId)
    ipcMain.handle('get-msg-by-id', getMsgById)
    win.loadFile('./pages/index.html')
})