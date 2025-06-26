const {app, BrowserWindow, ipcMain, dialog} = require('electron')
const path = require('path')
process.env['ELECTRON_DISABLE_SECURITY_WARNINGS'] = 'true';

const axiosApi = require("./axiosApi")

// 文件选择对话框
function openFile() {
    return dialog.showOpenDialog({
        filters: [{ name: 'Images', extensions: ['jpg', 'png', 'heic'] }],
        properties: ['openFile']
    }).then(result => {
        return result.canceled ? null : result.filePaths[0];
    }).catch(console.error);
}

app.on('ready', () => {
    const win = new BrowserWindow({
        width: 1280,
        height: 720,
        minWidth: 1280,
        minHeight: 720,
        autoHideMenuBar: true,
        webPreferences:{
            preload:path.resolve(__dirname, './preload.js'),

            // webSecurity: false14
        }
    })
    win.setAspectRatio(16 / 9)
    ipcMain.handle('file-open', openFile)
    ipcMain.handle('post-img', axiosApi.postImg)
    ipcMain.handle('save-work', axiosApi.saveWork)
    ipcMain.handle('get-all-id', axiosApi.getAllId)
    ipcMain.handle('get-msg-by-id', axiosApi.getMsgById)
    ipcMain.handle('get-id-by-time', axiosApi.getIdByTime)
    ipcMain.handle('del-work', axiosApi.delWork)
    ipcMain.handle('edit-work', axiosApi.editWork)
    win.loadFile('src/pages/index.html')
})