var map = new AMap.Map("map", {
    zoom: 12
})

var workId = 0
var isStarted = false
var navg = null
var pathSimplifierIns = null

// 分页配置
const rowsPerPage = 5
let currentPage = 1
var filteredData = []
var idToMarker

var trip
var outputPath

// DOM元素
const naviBar = document.getElementById('naviBar')
const midWindow = document.getElementById('midWindow')
const tableBody = document.getElementById('tableBody')
const prevPageBtn = document.getElementById('prevPage')
const nextPageBtn = document.getElementById('nextPage')
const firstPageBtn = document.getElementById('firstPage')
const lastPageBtn = document.getElementById('lastPage')
const pageNumbers = document.getElementById('pageNumbers')
const listBtn = document.getElementById('listBtn')
const editBtn = document.getElementById('editBtn')
const backBtn = document.getElementById('return')
const generate = document.getElementById('generate')
const mapContainer = document.getElementById('mapContainer')
const listContainer = document.getElementById('listContainer')
const workEdit = document.getElementById('workEdit')
const workImg = document.getElementById('workImg')
const workContent = document.getElementById('workContent')
const postPicture = document.getElementById('postPicture')
const saveWork = document.getElementById('saveWork')
const editWork = document.getElementById('editWork')
const delWork = document.getElementById('delWork')
const travelSwitch = document.getElementById('travelSwitch')
const detailedMsg = document.getElementById('detailedMsg')
const showMsg = document.getElementById('showMsg')
const startTime = document.getElementById('startTime')
const endTime = document.getElementById('endTime')
const searchBtn = document.getElementById('searchBtn')
const refrashBtn = document.getElementById('refrashBtn')
const cfRtnBtn = document.getElementById('cfRtnBtn')
const cfBtn = document.getElementById('cfBtn')
const confirmWindow = document.getElementById('confirmWindow')
const download = document.getElementById('download')
const downloadWindow = document.getElementById('downloadWindow')
const posOption = document.getElementById('posOption')
const timeOption = document.getElementById('timeOption')
const msgOption = document.getElementById('msgOption')
const word = document.getElementById('word')
const pdf = document.getElementById('pdf')
const png = document.getElementById('png')
const choosePathBtn = document.getElementById('choosePathBtn')
const dldPath = document.getElementById('dldPath')
const returnBtn = document.getElementById('returnBtn')
const downloadBtn = document.getElementById('downloadBtn')

listBtn.addEventListener('click', function() {
    listContainer.classList.toggle('hidden')
    mapContainer.classList.toggle('hidden')
    listBtn.classList.toggle('active')
})

editBtn.addEventListener('click', function() {
    workEdit.classList.remove('hidden');
    renderWork(0)
})

backBtn.addEventListener('click', function() {
    init()
    workEdit.classList.add('hidden')
    naviBar.classList.add('default')
    midWindow.classList.add('default')
})

generate.addEventListener('click', async function() {
    if (mapContainer.getAttribute('class').indexOf('hidden') > -1) {
        mapContainer.classList.remove('hidden')
        listContainer.classList.add('hidden')
        listBtn.classList.remove('active')
    }
    if (isStarted) {
        isStarted = false
        navg.stop()
        navg.destroy()
    } else {
        isStarted = true
        const path = []

        for (const id of [...filteredData].reverse()) {
            try {
                const msgResponse = await elt.ipcRenderer.invoke('get-msg-by-id', id);
                const msg = msgResponse.data;
                
                if (!msg || !msg.gcjLon || !msg.gcjLat) {
                    console.error(`Invalid coordinates for ID ${id}:`, msg);
                    continue;
                }
                
                path.push([msg.gcjLon, msg.gcjLat]);
            } catch (error) {
                console.error(`Error processing ID ${id}:`, error);
            }
        }

        if (path.length > 0) {

            AMapUI.load(['ui/misc/PathSimplifier'], function(PathSimplifier) {

                if (!PathSimplifier.supportCanvas) {
                    alert('当前环境不支持 Canvas！');
                    return;
                }

                let pathSimplifierIns = new PathSimplifier({
                    zIndex: 100,
                    map: map,
                    getPath: function(pathData, pathIndex) {
                        return pathData.path;
                    },
                    renderOptions: {
                        pathLineStyle: {
                            strokeStyle: 'rgba(255,0,0,0)',
                            lineWidth: 0,
                        }
                    }
                });

                pathSimplifierIns.setData([{
                    name: '轨迹0',
                    path: path
                }]);

                navg = pathSimplifierIns.createPathNavigator(0,
                    {
                        loop: true,
                        speed: 1000
                    });

                navg.start();

            });
        }
    }
})

postPicture.addEventListener('click', async function() {
    try {
        let picturePath = await elt.ipcRenderer.invoke('file-open')
        if (!picturePath) {
            return
        }
        console.log(picturePath)
        trip = (await elt.ipcRenderer.invoke('post-img', picturePath)).data
        console.log(trip)
        workImg.src = trip.previewPath
        workImg.classList.remove('hidden')

        detailedMsg.innerHTML = `
            经度：${trip.longitude} <br/>
            纬度：${trip.latitude} <br/>
            拍摄地点：${trip.location} <br/>
            制造商：${trip.make} <br/>
            `

        if (!trip.gcjLon || !trip.gcjLat) {
            trip.gcjLon = 116.3912
            trip.gcjLat = 39.9
        }

        let icon = new AMap.Icon({
            image: "../../public/resourse/Map pin.png",
        });

        const marker = new AMap.Marker({
            position: new AMap.LngLat(trip.gcjLon, trip.gcjLat),
            icon: icon,
            offset: new AMap.Pixel(-11, -25),
            draggable: true,
            cursor: 'move',
        });

        marker.on('dragend', function(e) {
            const position = e.target.getPosition();
            trip.gcjLat = position.lat
            trip.gcjLon = position.lng
            console.log(position.lng, position.lat);
        });

        map.clearMap()
        map.add(marker)
        map.setFitView(marker)
        saveWork.classList.remove('disable')

        travelSwitch.style.pointerEvents = 'auto'
        showMsg.classList.remove('disable')
    } catch (err) {
        console.error('操作失败:', err)
    }
});

saveWork.addEventListener('click', async function() {
    editWork.classList.remove('hidden')
    saveWork.classList.add('hidden')
    download.classList.remove('disable')
    trip.content = workContent.value
    console.log(trip.content)
    if (!trip.id) {
        if (!trip.location) {
            await new Promise((resolve, reject) => {
                AMap.plugin("AMap.Geocoder", function() {
                    const geocoder = new AMap.Geocoder();
                    geocoder.getAddress([trip.gcjLon, trip.gcjLat], function(status, result) {
                        if (status === "complete" && result.info === "OK") {
                            trip.location = result.regeocode.formattedAddress;
                            console.log("获取位置成功:", trip.location);
                            resolve();
                        } else {
                            console.error("地理编码失败:", result);
                            trip.location = "未知位置";
                            resolve();
                        }
                    });
                });
            });
        }
        console.log(trip.location)

        const travelData = {
            latitude: trip.latitude,
            longitude: trip.longitude,
            gcjLat: trip.gcjLat,
            gcjLon: trip.gcjLon,
            location: trip.location,
            takenTime: trip.takenTime,
            make: trip.make,
            model: trip.model,
            type: trip.type,
            width: trip.width,
            height: trip.height,
            exposureTime: trip.exposureTime,
            fnumber: trip.fnumber,
            iso: trip.iso,
            content: trip.content
        }
        const previewPath = trip.previewPath
        console.log(travelData.location)
        const response = await elt.ipcRenderer.invoke('save-work', previewPath, travelData)
        console.log(response)
    } else {
        const response = await elt.ipcRenderer.invoke('edit-work', workId, trip.content)
        console.log(response)
    }
    init()
})

editWork.addEventListener('click', function() {
    editWork.classList.add('hidden')
    saveWork.classList.remove('hidden')
    saveWork.classList.remove('disable')
    download.classList.add('disable')
    workContent.removeAttribute('readonly', true)
})

cfRtnBtn.addEventListener('click', function() {
    confirmWindow.classList.remove('active')
})

cfBtn.addEventListener('click', async function() {
    if (workId) {
        const response = await elt.ipcRenderer.invoke('del-work', workId)
        console.log(response)
        workEdit.classList.add('hidden')
        naviBar.classList.add('default')
        midWindow.classList.add('default')
        init()
    }
    confirmWindow.classList.remove('active')
})

delWork.addEventListener('click', function() {
    confirmWindow.classList.add('active')
})

travelSwitch.addEventListener('click', function() {
    workImg.classList.toggle('hidden')
    workContent.classList.toggle('hidden')
    travelSwitch.classList.toggle('active')
})

showMsg.addEventListener('click', function() {
    detailedMsg.classList.toggle('hidden')
})

searchBtn.addEventListener('click', async function() {
    const stTime = startTime.value
    const edTime = endTime.value

    if (stTime && edTime) {
        filteredData = (await elt.ipcRenderer.invoke('get-id-by-time', stTime, edTime)).data
        await loading()
        await renderTable()
    } else {
        alert('请先选择日期')
    }
})

refrashBtn.addEventListener('click', function() {
    init()
})

download.addEventListener('click', function() {
    downloadWindow.classList.add('active')
        dldPath.textContent = outputPath
})

posOption.addEventListener('click', function() {
    posOption.classList.toggle('selected')
})

timeOption.addEventListener('click', function() {
    timeOption.classList.toggle('selected')
})

msgOption.addEventListener('click', function() {
    msgOption.classList.toggle('selected')
})

word.addEventListener('click', function() {
    word.classList.add('selected')
    pdf.classList.remove('selected')
    png.classList.remove('selected')
})

pdf.addEventListener('click', function() {
    pdf.classList.add('selected')
    word.classList.remove('selected')
    png.classList.remove('selected')
})

png.addEventListener('click', function() {
    png.classList.add('selected')
    pdf.classList.remove('selected')
    word.classList.remove('selected')
})

choosePathBtn.addEventListener('click', async function() {
    const response = await elt.ipcRenderer.invoke('choose-path')
    console.log(response)
    if (response.canceled === false) {
        outputPath = response.filePaths[0]
        console.log(666)
        dldPath.textContent = outputPath
    }
})

returnBtn.addEventListener('click', function() {
    downloadWindow.classList.remove('active')
})

downloadBtn.addEventListener('click', async function() {
    downloadWindow.classList.remove('active')
    let id, location, time, param, type, path

    id = workId

    if (posOption.getAttribute('class').indexOf('selected') > -1) {
        location = true 
    } else {
        location = false
    }

    if (timeOption.getAttribute('class').indexOf('selected') > -1) {
        time = true 
    } else {
        time = false
    }

    if (msgOption.getAttribute('class').indexOf('selected') > -1) {
        param = true
    } else {
        param = false
    }

    if (word.getAttribute('class').indexOf('selected') > -1) {
        type = 'WORD'
    } else if (pdf.getAttribute('class').indexOf('selected') > -1) {
        type = 'PDF'
    } else {
        type = 'PNG'
    }

    path = outputPath

    const response = (await elt.ipcRenderer.invoke('export', id, location, time, param, type, path)).data
    console.log(response)
})

// 初始化
async function init() {
    map.clearMap()
    await loadData()
    await loading()
    await renderTable()
    setupEventListeners()
    updatePagination()
}

async function loadData() {
    filteredData = (await elt.ipcRenderer.invoke('get-all-id')).data
}

async function loading() {
    try {
        const markers = []
        idToMarker = new Map()
        for (const id of filteredData) {
            try {
                const msgResponse = await elt.ipcRenderer.invoke('get-msg-by-id', id);
                const msg = msgResponse.data;
                
                if (!msg || !msg.longitude || !msg.latitude) {
                    console.error(`Invalid coordinates for ID ${id}:`, msg);
                    continue;
                }
                
                let icon = new AMap.Icon({
                    image: "../../public/resourse/Map pin.png",
                });
                
                const marker = new AMap.Marker({
                    position: new AMap.LngLat(msg.gcjLon, msg.gcjLat),
                    icon: icon,
                    offset: new AMap.Pixel(-11, -25),
                });

                idToMarker.set(id, marker)
                
                marker.on('click', () => {
                    renderWork(id)
                });
                
                map.add(marker);
                markers.push(marker);
                            
                if (markers.length > 0) {
                    map.setFitView(markers);
                }
            } catch (error) {
                console.error(`Error processing ID ${id}:`, error);
            }
        }
    } catch (error) {
        console.error("Error in loadData:", error);
    }
}

// 渲染表格
async function renderTable() {
    tableBody.innerHTML = '';
    
    const startIndex = (currentPage - 1) * rowsPerPage;
    const endIndex = startIndex + rowsPerPage;
    const pageData = filteredData.slice(startIndex, endIndex);
    
    for(const id of pageData) {
        const response = await elt.ipcRenderer.invoke('get-msg-by-id', id)
        const msg = response.data
        console.log(msg)
        const tr = document.createElement('tr');
        
        tr.innerHTML = `
            <td>${String(msg.takenTime)}</td>
            <td>${msg.location}</td>
            <td>${msg.content}</td>
        `;

        tr.addEventListener('click', async function() {
            await renderWork(id)
        })
        
        tableBody.appendChild(tr);
    }
    
    // 更新分页信息
    pageNumbers.textContent = `${currentPage} / ${Math.ceil(filteredData.length / rowsPerPage)}`
}

// 设置事件监听器
function setupEventListeners() {
    // 分页按钮
    prevPageBtn.addEventListener('click', () => changePage(-1));
    nextPageBtn.addEventListener('click', () => changePage(1));
    firstPageBtn.addEventListener('click', () => goToPage(1));
    lastPageBtn.addEventListener('click', () => goToPage(Math.ceil(filteredData.length / rowsPerPage)));
}

// 改变页码
function changePage(direction) {
    const totalPages = Math.ceil(filteredData.length / rowsPerPage);
    const newPage = currentPage + direction;
    
    if (newPage > 0 && newPage <= totalPages) {
        currentPage = newPage;
        renderTable();
        updatePagination();
    }
}

// 跳转到指定页
function goToPage(page) {
    const totalPages = Math.ceil(filteredData.length / rowsPerPage);
    
    if (page >= 1 && page <= totalPages) {
        currentPage = page;
        renderTable();
        updatePagination();
    }
}

// 更新分页控件
function updatePagination() {
    const totalPages = Math.ceil(filteredData.length / rowsPerPage);
    
    // 更新按钮状态
    prevPageBtn.disabled = currentPage === 1;
    nextPageBtn.disabled = currentPage === totalPages;
    firstPageBtn.disabled = currentPage === 1;
    lastPageBtn.disabled = currentPage === totalPages;
    
    const maxVisiblePages = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
    
    // 调整起始页码
    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }
}

// 加载游记编辑界面
async function renderWork(id) {
    workId = id
    naviBar.classList.remove('default')
    midWindow.classList.remove('default')
    workEdit.classList.remove('hidden')
    detailedMsg.classList.add('hidden')
    travelSwitch.classList.remove('active')
    if (!id) {
        /*-----------界面修改-----------*/
        workImg.classList.add('hidden')
        workContent.classList.add('hidden')
        saveWork.classList.remove('hidden')
        editWork.classList.add('hidden')
        workContent.removeAttribute('readonly', true)
        postPicture.classList.remove('disable')
        delWork.classList.add('disable')
        travelSwitch.style.pointerEvents = 'none'
        showMsg.classList.add('disable')
        saveWork.classList.add('disable')
        download.classList.add('disable')

        /*-----------数据修改-----------*/
        workContent.value = ""
        workImg.src = ""
        detailedMsg.innerHTML = ''
    } else {
        /*-----------界面修改-----------*/
        workImg.classList.remove('hidden')
        workContent.classList.add('hidden')
        saveWork.classList.add('hidden')
        editWork.classList.remove('hidden')
        workContent.setAttribute('readonly', true)
        postPicture.classList.add('disable')
        delWork.classList.remove('disable')
        travelSwitch.style.pointerEvents = 'auto'
        showMsg.classList.remove('disable')
        map.setFitView(idToMarker.get(id))
        download.classList.remove('disable')

        /*-----------数据修改-----------*/
        const response = await elt.ipcRenderer.invoke('get-msg-by-id', id)
        trip = response.data
        workImg.src = trip.imagePath
        workContent.value = trip.content
        detailedMsg.innerHTML = `
            经度：${trip.longitude} <br/>
            纬度：${trip.latitude} <br/>
            拍摄地点：${trip.location} <br/>
            制造商：${trip.make} <br/>
            `
    }
}

// 初始化表格
document.addEventListener('DOMContentLoaded', init)