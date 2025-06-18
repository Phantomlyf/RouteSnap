var map = new AMap.Map("map", {
    zoom: 12
})

var geocoder
var workId = 0

// 分页配置
const rowsPerPage = 5
let currentPage = 1
var filteredData = []

var trip = {
    picturePath: null,
    lat: null,
    lng: null,
    detailedPos: null,
    context: null
}

map.plugin('AMap.Geocoder', function() {
    geocoder = new AMap.Geocoder()
})

// DOM元素
const tableBody = document.getElementById('tableBody')
const prevPageBtn = document.getElementById('prevPage')
const nextPageBtn = document.getElementById('nextPage')
const firstPageBtn = document.getElementById('firstPage')
const lastPageBtn = document.getElementById('lastPage')
const currentPageEl = document.getElementById('currentPage')
const totalPagesEl = document.getElementById('totalPages')
const pageNumbersEl = document.getElementById('pageNumbers')
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

console.log(listBtn)

listBtn.addEventListener('click', function() {
    listContainer.classList.toggle('hidden')
    mapContainer.classList.toggle('hidden')
    listBtn.classList.toggle('active')
    console.log(666)
})

editBtn.addEventListener('click', function() {
    workEdit.classList.remove('hidden');
    renderWork(0)
})

backBtn.addEventListener('click', function() {
    workEdit.classList.add('hidden');
})

// generate.addEventListener('click', function() {
//     mapPage.classList.remove('hidden');
//     workList.classList.add('hidden');
// })

postPicture.addEventListener('click', async function() {
  try {
    trip.picturePath = await elt.ipcRenderer.invoke('file-open');
    if (!trip.picturePath) return;
    workImg.src = trip.picturePath;
    
    const imgMsg = await elt.ipcRenderer.invoke('post-img', trip.picturePath)
    trip.lat = imgMsg.data.latitude
    trip.lng = imgMsg.data.longitude
    
    if (trip.lat == null) {
        trip.detailedPos = null
    } else {
        trip.detailedPos = await new Promise((resolve) => {
        geocoder.getAddress([trip.lng, trip.lat], (status, result) => {
            console.log(status)
            console.log(result)
          if (status === 'complete' && result.info == 'OK') {
            resolve(result.regeocode.formattedAddress)
          } else {
            resolve(null) // 或 reject()
          }
        });
      });
    }
    console.log('上传结果:', imgMsg);
    console.log('详细地址:', trip.detailedPos)
    } catch (err) {
        console.error('操作失败:', err)
    }
});

saveWork.addEventListener('click', async function() {
    editWork.classList.remove('hidden')
    saveWork.classList.add('hidden')
    trip.context = workContent.value
    console.log(trip.context)
    if (workId) {
        const response = await elt.ipcRenderer.invoke('save-work', trip.picturePath, trip.detailedPos, trip.context)
        console.log(response)
    } else {

    }
})

editWork.addEventListener('click', function() {
    editWork.classList.add('hidden')
    saveWork.classList.remove('hidden')
    postPicture.removeAttribute('disabled', true)
    workContent.removeAttribute('readonly', true)
})

delWork.addEventListener('click', function() {

})

// 初始化
async function init() {
    return
    await loadData()
    await renderTable()
    setupEventListeners()
    updatePagination()
}

async function loadData() {
    try {
        const idsResponse = await elt.ipcRenderer.invoke('get-all-id');
        filteredData = idsResponse.data;
        
        const path = [];
        const markers = [];
        
        for (const id of filteredData) {
            try {
                const msgResponse = await elt.ipcRenderer.invoke('get-msg-by-id', id);
                const msg = msgResponse.data;
                
                console.log("Marker data:", msg);
                
                if (!msg || !msg.longitude || !msg.latitude) {
                    console.error(`Invalid coordinates for ID ${id}:`, msg);
                    continue;
                }
                
                function getFileUrl(filePath) {
                    let path = filePath.replace(/\\/g, '/');
                    if (!path.startsWith('file://')) {
                        path = `file:///${path}`;
                    }
                    return path;
                }
                
                let icon = null;
                if (msg.imagePath) {
                    icon = new AMap.Icon({
                        size: new AMap.Size(40, 50),
                        image: getFileUrl(msg.imagePath),
                        imageOffset: new AMap.Pixel(0, -60),
                        imageSize: new AMap.Size(40, 50)
                    });
                }
                
                // 6. 创建标记
                const marker = new AMap.Marker({
                    position: new AMap.LngLat(Number(msg.longitude), 
                    Number(msg.latitude)),
                    icon: icon || undefined
                });
                
                path.push([Number(msg.longitude), Number(msg.latitude)]);
                
                marker.on('click', () => {
                    renderWork(id)
                });
                
                map.add(marker);
                markers.push(marker);
                
            } catch (error) {
                console.error(`Error processing ID ${id}:`, error);
            }
        }
        
        if (path.length > 0) {
            const polyline = new AMap.Polyline({
                path: path,
                strokeColor: "#3366FF",
                strokeWeight: 5,
                strokeStyle: "solid",
                lineJoin: 'round'
            });
            map.add(polyline);
        }
        
        if (markers.length > 0) {
            map.setFitView(markers);
        }
        
    } catch (error) {
        console.error("Error in loadData:", error);
    }
}

// 渲染表格
async function renderTable() {
    console.log(1)
    tableBody.innerHTML = '';
    
    const startIndex = (currentPage - 1) * rowsPerPage;
    const endIndex = startIndex + rowsPerPage;
    const pageData = filteredData.slice(startIndex, endIndex);
    
    pageData.forEach(async id => {
        console.log(id)
        const response = await elt.ipcRenderer.invoke('get-msg-by-id', id)
        const msg = response.data
        console.log(msg)
        const tr = document.createElement('tr');
        
        tr.innerHTML = `
            <td>${msg.takeTime}</td>
            <td>${msg.location}</td>
            <td>${msg.content}</td>
        `;

        tr.addEventListener('click', async function() {
            await renderWork(id)
        })
        
        tableBody.appendChild(tr);
    });
    
    // 更新分页信息
    currentPageEl.textContent = currentPage;
    totalPagesEl.textContent = Math.ceil(filteredData.length / rowsPerPage);
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
    
    // 生成页码按钮
    pageNumbersEl.innerHTML = '';
    
    const maxVisiblePages = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
    
    // 调整起始页码
    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.textContent = i;
        pageBtn.classList.toggle('active', i === currentPage);
        pageBtn.addEventListener('click', () => goToPage(i));
        pageNumbersEl.appendChild(pageBtn);
    }
}

// 加载游记编辑界面
async function renderWork(id) {
    workId = id
    workEdit.classList.remove('hidden')
    if (!id) {
        /*-----------界面修改-----------*/
        workImg.classList.add('hidden')
        saveWork.classList.remove('hidden')
        editWork.classList.add('hidden')
        workContent.removeAttribute('readonly', true)
        postPicture.removeAttribute('disabled', true)
        delWork.setAttribute('disabled', true)

        /*-----------数据修改-----------*/
        workContent.value = ""
        workImg.src = ""
    } else {
        /*-----------界面修改-----------*/
        workImg.classList.remove('hidden')
        saveWork.classList.add('hidden')
        editWork.classList.remove('hidden')
        workContent.setAttribute('readonly', true)
        postPicture.setAttribute('disabled', true)
        delWork.removeAttribute('disabled', true)

        /*-----------数据修改-----------*/
        const response = await elt.ipcRenderer.invoke('get-msg-by-id', id)
        const msg = response.data
        workImg.src = msg.imagePath
        workContent.value = msg.content
        trip.picturePath = msg.imagePath
        trip.detailedPos = msg.location
    }
}

// 初始化表格
document.addEventListener('DOMContentLoaded', init)