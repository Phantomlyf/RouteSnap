const axios = require('axios')
const fs = require('fs')
const FormData = require('form-data')
const { url } = require('inspector')
const urlPath = 'http://localhost:4399'

async function postImg(_, filePath) {
    if (!filePath) return { error: "No file selected" }
    
    try {
        const encodedPath = encodeURIComponent(filePath.replace(/\\/g, '/'))
        const response = await axios.get(`http://localhost:4399/upload?imagePath=${encodedPath}`);
        return response.data
    } catch (error) {
        console.error('Upload failed:', error)
        return { error: error.message }
    }
}


async function saveWork(_, previewPath, travelData) {
    try {
        const encodedPath = encodeURIComponent(previewPath.replace(/\\/g, '/'))
        const response = await axios.post(`http://localhost:4399/travel/upload2?previewPath=${encodedPath}`, travelData, {
            headers: {
                 'Content-Type': 'application/json'
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
        const response = await axios.get('http://localhost:4399/travel/listId')
        return response.data
    } catch(e) {
        console.error(e)
        return {error: e.message}
    }
}

async function getMsgById(_, id) {
    try {
        const response = await axios.get(`${urlPath}/travel/${id}`)
        return response.data
    } catch(e) {
        console.error(e)
        return {error: e.message}
    }
}

async function getIdByTime(_, stTime, edTime) {
    try {
        const response = await axios.get(`${urlPath}/travel/listBycase?startTime=${stTime}&endTime=${edTime}`)
        console.log(response.data)
        return response.data
    } catch(e) {
        console.error(e)
        return {error : e.message}
    }
}

async function delWork(_, id) {
    try {
        const response = await axios.delete(`${urlPath}/travel/delete/${id}`)
        return response.data
    } catch(e) {
        console.error(e)
        return {error : e.message}
    }
}

async function editWork(_, id, content) {
    try {
        console.log(id, content)
        const response = await axios.put(`${urlPath}/travel/change`, {
            id: id,
            content: content
        }, {
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        })
        return response.data
    } catch(e) {
        console.error(e)
        return {error: e.message}
    }
}

module.exports = {
    postImg, saveWork, getAllId, getMsgById, getIdByTime, delWork, editWork
}