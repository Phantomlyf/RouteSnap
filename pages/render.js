var map = new AMap.Map("map", {
    zoom: 12
})

var geocoder
const tableData = [
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 0, lat: 33.9, lng: 111.6},
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 1, lat: -33.9, lng: 111.6},
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 2, lat: 43.9, lng: 111.6},
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 3, lat: 53.9, lng: 111.6},
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 4, lat: 63.9, lng: 111.6},
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 5, lat: 73.9, lng: 111.6},
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 6, lat: -13.9, lng: 111.6},
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 7, lat: -23.9, lng: 111.6},
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 8, lat: -43.9, lng: 111.6},
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 9, lat: -53.9, lng: 111.6},
    {time: "2024/11/12", position: "湖北省武汉市", context: "1234123451235", id: 9, lat: -63.9, lng: 111.6},
]
// 分页配置
const rowsPerPage = 5
let currentPage = 1
let filteredData = [...tableData]

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
const backBtn = document.getElementById('backBtn')
const generate = document.getElementById('generate')
const mapPage = document.getElementById('map')
const workList = document.getElementById('workList')
const workEdit = document.getElementById('workEdit')
const workImg = document.getElementById('workImg')
const workContent = document.getElementById('workContent')
const postPicture = document.getElementById('postPicture')
const saveWork = document.getElementById('saveWork')
const editWork = document.getElementById('editWork')
const delWork = document.getElementById('delWork')

listBtn.addEventListener('click', function() {
    workList.classList.toggle('hidden');
    mapPage.classList.toggle('hidden');
})

editBtn.addEventListener('click', function() {
    workEdit.classList.toggle('hidden');
})

backBtn.addEventListener('click', function() {
    workEdit.classList.add('hidden');
})

generate.addEventListener('click', function() {
    mapPage.classList.remove('hidden');
    workList.classList.add('hidden');
})

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
    const response = await elt.ipcRenderer.invoke('save-work', trip.picturePath, trip.detailedPos, trip.context)
    console.log(response)
})

editWork.addEventListener('click', function() {
    editWork.classList.add('hidden')
    saveWork.classList.remove('hidden')
    postPicture.setAttribute('disabled', false)
    workContent.setAttribute('readonly', false)
})

delWork.addEventListener('click', function() {

})

// 初始化
function init() {
    loadData();
    renderTable();
    setupEventListeners();
    updatePagination();
}

function loadData() {
    var path = new Array();

    // filteredData = elt.ipcRenderer.invoke('get-all-id')

    filteredData.forEach(pos => {
        let icon = new AMap.icon({
            size: new AMap.Size(40, 50),    // 图标尺寸
            image: '',  // Icon的图像
            imageOffset: new AMap.Pixel(0, -60),  // 图像相对展示区域的偏移量，适于雪碧图等
            imageSize: new AMap.Size(40, 50)   // 根据所设置的大小拉伸或压缩图片
        })
        let marker = new AMap.Marker({
            position: new AMap.LngLat(pos.lng, pos.lat),
            icon: icon
        })
        path.push([pos.lng, pos.lat])
        marker.on('click', function() {
            workEdit.classList.remove('hidden')
        })
        map.add(marker);
    })
    // 创建折线
    const polyline = new AMap.Polyline({
        path: path,             // 路径坐标
        strokeColor: "#3366FF", // 线颜色
        strokeWeight: 5,        // 线宽
        strokeStyle: "solid",   // 线样式
        lineJoin: 'round'       // 折点处理方式
    });

    map.add(polyline);
    map.setFitView();
}

// 渲染表格
function renderTable() {
    tableBody.innerHTML = '';
    
    const startIndex = (currentPage - 1) * rowsPerPage;
    const endIndex = startIndex + rowsPerPage;
    const pageData = filteredData.slice(startIndex, endIndex);
    
    pageData.forEach(row => {
        const tr = document.createElement('tr');
        
        tr.innerHTML = `
            <td>${row.time}</td>
            <td>${row.position}</td>
            <td>${row.context}</td>
        `;

        tr.addEventListener('click', function() {
            workEdit.classList.remove('hidden');
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
function renderWork(id) {
    workEdit.classList.remove('hidden')
    if (!id) {
        workImg.classList.add('hidden')
        workContent.setAttribute('readonly', false)
        workContent.context = ""
        saveWork.classList.remove('hidden')
        editWork.classList.add('hidden')
        postPicture.setAttribute('disabled', false)
        delWork.setAttribute('disabled', true)
    } else {
        const Msg = elt.ipcRenderer.invoke('get-msg-by-id', id)
        workImg.classList.remove('hidden')
        workContent.setAttribute('readonly', true)
        workContent.context = Msg.context
        saveWork.classList.add('hidden')
        editWork.classList.remove('hidden')
        postPicture.setAttribute('disabled', true)
        delWork.setAttribute('disabled', false)
    }
}

// 初始化表格
document.addEventListener('DOMContentLoaded', init)