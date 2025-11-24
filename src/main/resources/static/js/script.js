// ============================================================
// 1. CẤU HÌNH CHUNG
// ============================================================
const VN_MAP_URL = "/data/vietnam.geojson.json";
let mapChart;

// ============================================================
// 2. KHỞI TẠO BẢN ĐỒ
// ============================================================
async function initMap() {
  showLoading(true);
  try {
    if (window.proj4) {
      window.proj4.defs(
        "EPSG:32648",
        "+proj=utm +zone=48 +datum=WGS84 +units=m +no_defs"
      );
    }

    const response = await fetch(VN_MAP_URL);
    const topology = await response.json();
    const data = Highcharts.geojson(topology);

    // Gán dữ liệu giả lập để Highcharts hiểu đây là các vùng có thể tương tác
    data.forEach((d, i) => {
      d.value = i;
    });

    mapChart = Highcharts.mapChart("map", {
      chart: {
        proj4: window.proj4,
        backgroundColor: "#a4d4ff",

        // --- CẤU HÌNH KÉO THẢ DI CHUYỂN ---
        panning: { enabled: true, type: "xy" },
        marginRight: 80,
        marginBottom: 30,
      },
      title: {
        text: "Bản Đồ Thời Tiết Việt Nam",
        style: {
          fontSize: "18px",
          fontFamily: "Roboto",
          fontWeight: "bold",
          color: "#1e3a8a",
        },
      },
      subtitle: {
        text: "Dữ liệu: Tomorrow.io - Bản đồ nền: Highcharts",
        style: { fontSize: "12px", fontFamily: "Roboto" },
      },
      mapNavigation: {
        enabled: true,
        enableDoubleClickZoom: true,
        buttonOptions: {
          verticalAlign: "bottom",
          align: "right",
          x: 70,
          y: -100,
        },
      },
      exporting: {
        enabled: false,
      },
      colorAxis: {
        min: 0,
        minColor: "#E6E6E6",
        maxColor: "#0056b3",
      },
      legend: { enabled: false },

      plotOptions: {
        map: {
          allAreas: false,
          joinBy: null,
          borderColor: "#9ca3af",
          borderWidth: 0.5,
          states: {
            hover: {
              color: "#FACC15",
              borderColor: "#1E3A8A",
              borderWidth: 2,
              enabled: true,
              bringToFront: true,
            },
            select: {
              color: "#F97316",
              borderColor: "#fff",
              borderWidth: 2,
              bringToFront: true,
            },
          },
          dataLabels: {
            enabled: true,
            format: "{point.properties.name}",
            style: {
              fontSize: "9px",
              fontWeight: "normal",
              textOutline: "none",
              color: "#333",
            },
          },
        },
      },

      series: [
        // --- SERIES 1: CÁC TỈNH THÀNH ---
        {
          name: "Tỉnh thành",
          data: data,
          allowPointSelect: true,
          point: {
            events: {
              click: function () {
                const name = this.properties.name;
                console.log("Click vào tỉnh:", name);
                this.select(true, false);
                executeSearch(name);
              },
            },
          },
          tooltip: {
            headerFormat: "",
            pointFormat:
              '<span style="color:#1E3A8A">📍 {point.properties.name}</span>',
          },
        },

        // --- SERIES 2: BIỂN ĐẢO ---
        {
          type: "mappoint",
          name: "Biển Đảo",
          color: "#dc2626",
          zIndex: 1000,
          marker: {
            symbol: "circle",
            radius: 6,
            lineWidth: 2,
            lineColor: "#ffffff",
          },
          dataLabels: {
            enabled: true,
            format: "{point.name}",
            style: {
              color: "#b91c1c",
              fontWeight: "bold",
              fontSize: "11px",
              textOutline: "3px white",
            },
            y: -12,
            allowOverlap: true,
          },
          tooltip: {
            pointFormat: "<b>{point.name}</b><br>Việt Nam",
          },
          data: [
            {
              name: "Huyện đảo Hoàng Sa",
              geometry: {
                type: "Point",
                coordinates: [5011.5, 4616.5],
              },
              weatherLat: 16.5,
              weatherLon: 111.6,
            },
            {
              name: "Huyện đảo Trường Sa",
              geometry: {
                type: "Point",
                coordinates: [6012.5, 1009.5],
              },
              weatherLat: 8.6,
              weatherLon: 111.9,
            },
          ],
          events: {
            click: function (e) {
              fetchWeather(
                e.point.weatherLat,
                e.point.weatherLon,
                e.point.name
              );
            },
          },
        },
      ],
    });
  } catch (error) {
    console.error("Lỗi khởi tạo bản đồ:", error);
  } finally {
    showLoading(false);
  }
}

// ============================================================
// 3. LOGIC TÌM KIẾM & XỬ LÝ DỮ LIỆU (FULL)
// ============================================================

function removeAccents(str) {
  return str
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase();
}

async function executeSearch(query) {
  if (!query) return alert("Vui lòng nhập tên địa điểm!");
  let mapFound = false;
  if (mapChart && mapChart.series[0]) {
    const points = mapChart.series[0].points;
    const searchKey = removeAccents(query);
    const foundPoint = points.find((p) =>
      removeAccents(p.properties.name || "").includes(searchKey)
    );

    if (foundPoint) {
      foundPoint.select(true, false);
      foundPoint.zoomTo();
      mapFound = true;
      const lat = parseFloat(foundPoint.properties.latitude);
      const lon = parseFloat(foundPoint.properties.longitude);
      if (!isNaN(lat) && !isNaN(lon)) {
        fetchWeather(lat, lon, foundPoint.properties.name);
        return;
      }
    }
  }
  let searchQuery = query;
  if (!searchQuery.toLowerCase().includes("vietnam"))
    searchQuery += ", Vietnam";

  showLoading(true);
  try {
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
      searchQuery
    )}&countrycodes=vn&limit=1`;
    const res = await fetch(url);
    const results = await res.json();

    if (results.length > 0) {
      const { lat, lon, display_name } = results[0];
      const shortName = display_name.split(",")[0];
      fetchWeather(parseFloat(lat), parseFloat(lon), shortName);
    } else {
      if (!mapFound) alert(`Không tìm thấy địa điểm: ${query}`);
    }
  } catch (e) {
    console.error("Lỗi tìm kiếm:", e);
  } finally {
    showLoading(false);
  }
}

function triggerSearch() {
  const input = document.getElementById("search-input");
  executeSearch(input.value.trim());
}

function handleEnter(event) {
  if (event.key === "Enter") triggerSearch();
}

async function fetchWeather(lat, lng, locationName = "") {
  if (document.getElementById("loading").style.display === "none")
    showLoading(true);

  try {
    const encodedName = encodeURIComponent(locationName);
    const response = await fetch(
      `/api/weather/current?lat=${lat}&lon=${lng}&locationName=${encodedName}`
    );

    if (!response.ok) throw new Error(`Lỗi Server: ${response.status}`);
    const data = await response.json();

    updateSidebar(data);
    addToTable(data);
  } catch (error) {
    console.error(error);
    alert("Không thể lấy dữ liệu thời tiết.");
  } finally {
    showLoading(false);
  }
}
const fmt = (val, suffix = "") =>
  val !== null && val !== undefined ? `${val}${suffix}` : "--";
const fmtNum = (val, suffix = "") =>
  val !== null && val !== undefined
    ? `${Math.round(val * 100) / 100}${suffix}`
    : "--";

function updateSidebar(data) {
  const content = document.getElementById("weather-content");
  const recordedTime = data.recordedAt
    ? new Date(data.recordedAt).toLocaleString("vi-VN")
    : "Vừa cập nhật";
  const uvColor =
    data.uvIndex > 7
      ? "text-red-600"
      : data.uvIndex > 4
      ? "text-yellow-600"
      : "text-green-600";
  const aqiColor = data.aqi > 100 ? "text-red-600" : "text-green-600";

  content.innerHTML = `
    <div class="weather-main text-center border-b border-gray-200 pb-4 mb-4">
        <div class="text-xs text-gray-500 italic mb-1">Cập nhật: ${recordedTime}</div>
        <h3 class="city-name text-xl font-bold text-blue-900 mb-1"><i class="fas fa-map-marker-alt text-red-600"></i> ${
          data.name || "Vị trí"
        }</h3>
        <div class="weather-icon-wrapper text-6xl my-2">${
          data.icon || "🌍"
        }</div>
        <div class="temperature text-5xl font-bold text-blue-600">${fmtNum(
          data.temp,
          "°C"
        )}</div>
        <p class="description text-gray-500 capitalize font-medium mt-1">${
          data.conditionText || ""
        }</p>
        <div class="mt-2 text-sm bg-blue-50 inline-block px-3 py-1 rounded-full text-blue-600">Cảm giác: <b>${fmtNum(
          data.feelsLike,
          "°C"
        )}</b></div>
    </div>
    <div class="weather-details-grid grid grid-cols-2 gap-3 text-sm">
        <div class="col-span-2 text-xs font-bold text-gray-400 uppercase mt-2 border-b">Khí quyển</div>
        <div class="detail-item bg-gray-50 p-2 rounded"><span class="text-xs text-gray-500">Độ ẩm</span><span class="font-bold text-gray-700">${fmt(
          data.humidity,
          "%"
        )}</span></div>
        <div class="detail-item bg-gray-50 p-2 rounded"><span class="text-xs text-gray-500">Điểm sương</span><span class="font-bold text-gray-700">${fmtNum(
          data.dewPoint,
          "°C"
        )}</span></div>
        <div class="detail-item bg-gray-50 p-2 rounded"><span class="text-xs text-gray-500">Tầm nhìn</span><span class="font-bold text-gray-700">${fmtNum(
          data.visibility,
          " km"
        )}</span></div>
        <div class="detail-item bg-gray-50 p-2 rounded"><span class="text-xs text-gray-500">Áp suất</span><span class="font-bold text-gray-700">${fmtNum(
          data.pressureSurface,
          " hPa"
        )}</span></div>

        <div class="col-span-2 text-xs font-bold text-gray-400 uppercase mt-2 border-b">Mưa & Gió</div>
        <div class="detail-item bg-indigo-50 p-2 rounded"><span class="text-xs text-gray-500">Mưa (1h)</span><span class="font-bold text-indigo-700">${fmtNum(
          data.rain,
          " mm"
        )}</span></div>
        <div class="detail-item bg-indigo-50 p-2 rounded"><span class="text-xs text-gray-500">Xác suất</span><span class="font-bold text-indigo-700">${fmt(
          data.precipProbability,
          "%"
        )}</span></div>
        <div class="detail-item bg-green-50 p-2 rounded"><span class="text-xs text-gray-500">Gió</span><span class="font-bold text-green-600">${fmtNum(
          data.wind,
          " m/s"
        )}</span></div>
        <div class="detail-item bg-green-50 p-2 rounded"><span class="text-xs text-gray-500">Gió giật</span><span class="font-bold text-green-600">${fmtNum(
          data.windGust,
          " m/s"
        )}</span></div>

        <div class="col-span-2 text-xs font-bold text-gray-400 uppercase mt-2 border-b">Sức khỏe</div>
        <div class="detail-item bg-yellow-50 p-2 rounded"><span class="text-xs text-gray-500">UV Index</span><span class="font-bold ${uvColor}">${fmt(
    data.uvIndex
  )}</span></div>
        <div class="detail-item bg-gray-100 p-2 rounded"><span class="text-xs text-gray-500">AQI</span><span class="font-bold ${aqiColor}">${fmt(
    data.aqi
  )}</span></div>
    </div>`;
  document.getElementById("sidebar").classList.remove("hidden-sidebar");
}

function addToTable(data) {
  const tbody = document.getElementById("weather-table-body");
  const emptyRow = document.getElementById("empty-row");
  if (emptyRow) emptyRow.remove();
  const now = new Date().toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
  });
  const row = document.createElement("tr");
  const uvClass = data.uvIndex > 6 ? "text-red-600 font-bold" : "";
  const safeName = (data.name || "").replace(/'/g, "\\'");
  row.innerHTML = `
        <td class="text-gray-500 text-xs">${now}</td>
        <td class="font-bold text-blue-600 text-sm cursor-pointer" onclick="fetchWeather(${
          data.lat
        }, ${data.lng}, '${safeName}')">${data.name}</td>
        <td class="font-medium text-gray-800">${Math.round(data.temp)}°C</td>
        <td class="text-gray-600 text-xs">${fmtNum(data.feelsLike)}°C</td>
        <td class="text-gray-600 text-xs">${fmtNum(data.rain)}</td>
        <td class="text-gray-600 text-xs">${fmt(data.precipProbability)}%</td>
        <td class="text-gray-600 text-xs">${fmtNum(data.wind)}</td>
        <td class="${uvClass} text-xs">${fmt(data.uvIndex)}</td>
    `;
  tbody.prepend(row);
}

function clearTable() {
  document.getElementById(
    "weather-table-body"
  ).innerHTML = `<tr id="empty-row"><td colspan="8" class="empty-message">Đã xóa dữ liệu.</td></tr>`;
}
function closeSidebar() {
  document.getElementById("sidebar").classList.add("hidden-sidebar");
}
function showLoading(show) {
  document.getElementById("loading").style.display = show ? "flex" : "none";
}

window.onload = initMap;
