console.log('preload')
const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('elt', {
    ipcRenderer: {
        invoke: (channel, ...args) => ipcRenderer.invoke(channel, ...args)
    }
});