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
        const response = await axios.post(`http://localhost:4399/travel/upload2?previewPath=${previewPath}`, travelData, {
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
        const response = await axios.post(`${urlPath}/genTra?startTime=${stTime}&endTime=${edTime}`)
        return response.data
    } catch(e) {
        console.error(e)
        return {error : e.message}
    }
}

module.exports = {
    postImg, saveWork, getAllId, getMsgById, getIdByTime
}