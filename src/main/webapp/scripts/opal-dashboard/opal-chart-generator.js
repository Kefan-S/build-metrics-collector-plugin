const resultCodeMap = ["SUCCESS", "UNSTABLE", "FAILURE", "NOT_BUILT", "ABORT"]

let durationcalculate = function (value) {
  var mss= Number(value);
  var days = parseInt(mss / (1000 * 60 * 60 * 24) + 0.001 );
  var hours = parseInt((mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60) + 0.001);
  var minutes = parseInt((mss % (1000 * 60 * 60)) / (1000 * 60) + 0.001);
  var seconds = (mss % (1000 * 60)) / 1000;
  var time = "";
  if (days) {
    time = time + days + " d ";
  }
  if (hours) {
    time = time + hours + " h ";
  }
  if (minutes) {
    time = time + minutes + " m ";
  }
  if (seconds) {
    time = time + seconds.toFixed(2) + " s ";
  }
  return time;
};

let endTimeCalculate = function (data) {
  return Number(JSON.parse(data).startTime) + Number(JSON.parse(data).duration)
};

let timeStampToDateTranslator = function (value, index) {
  var date = new Date((endTimeCalculate(value)));
  var texts = [(date.getMonth() + 1), date.getDate()];
  if (index === 0) {
    texts.unshift(1900 + date.getYear());
  }
  return texts.join('/');
};

let timeStampToDateTimeTranslator = function (value) {
  var date = new Date(endTimeCalculate(value));
  var y = date.getFullYear();
  var m = date.getMonth() + 1;
  m = m < 10 ? ('0' + m) : m;
  var d = date.getDate();
  d = d < 10 ? ('0' + d) : d;
  var h = date.getHours();
  h = h < 10 ? ('0' + h) : h;
  var minute = date.getMinutes();
  var second = date.getSeconds();
  minute = minute < 10 ? ('0' + minute) : minute;
  second = second < 10 ? ('0' + second) : second;
  return y + '-' + m + '-' + d + ' ' + h + ':' + minute + ':' + second;
};

function lineChartOptionGenerator(chartName, data,
    yAxisName, xAxisName, yAxisField, toolTipFormat) {
  if(isNil(data)) return null;
  return {
    grid: {
      left: 120,
      right:120,
    },
    title: {
      text: chartName,
      left: "center",
      top: 'bottom'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        animation: false
      },
      formatter: toolTipFormat
    },
    xAxis: {
      name: xAxisName,
      type: 'category',
      data: data.map(xdata => JSON.stringify(xdata)),
      axisLabel: {
        formatter: timeStampToDateTranslator
      }
    },
    yAxis: {
      name: yAxisName,
      type: 'value',
      axisLabel: {
        formatter: durationcalculate
      }
    },
    series: [{
      data: data.map(ydata => ydata[yAxisField]),
      type: 'line',
      smooth: true
    }]
  }
}

function gaugeChartOptionGenerator(chartName, data, formatter, metricsName,
    toolTipFormatter,
    color = [[0.2, '#91c7ae'], [0.8, '#63869e'], [1, '#c23531']]) {
  if(isNil(data)) return null;
  return {
    title: {
      text: chartName,
      left: "center",
      top: 'bottom'
    },
    tooltip: {
      formatter: toolTipFormatter
    },
    series: [
      {
        radius: "90%",
        name: metricsName,
        type: 'gauge',
        detail: {formatter: formatter},
        data: [{value: data}],
        axisLine: {            // 坐标轴线
          lineStyle: {       // 属性lineStyle控制线条样式
            color: color
          }
        }
      }
    ]
  }
}

function deployTimeDistributionChartOptionGenerator(data) {
  let series = deployFrequencyDistributionCalculate(data);
  if(!series.reduce((a,b) => a + b)) return null;
  return {
    grid: {
      left: 120,
      right: 120,

    },
    title: {
      text: 'Deploy Time Distribution',
      left: "center",
      top: 'bottom'
    },
    tooltip: {
      trigger: 'axis',
      formatter: function (params) {
        return `deploy frequency: ${params[0].value}<br/>`+
            `time: ${params[0].axisValue}:00 - ${parseInt(params[0].axisValue)+1}:00`
      }
    },
    toolbox: {
      show: true
    },
    calculable: true,
    xAxis: [
      {
        name: 'Time',
        type: 'category',
        data: Array.from(new Array(24).keys()),
        axisLabel: {
          formatter: value => `${value}:00`
        }
      }
    ],
    yAxis: [
      {
        name: 'deploy frequency',
        type: 'value'
      }
    ],
    series: [
      {
        name: 'deploy frequency',
        type: 'bar',
        data: series,
        markPoint: {
          data: [
            {type: 'max', name: 'max'},
          ]
        }
      }
    ]
  }
}

function deployFrequencyDistributionCalculate(data) {
  let distributions = new Array(24).fill(0);
  data.buildInfos.map(data => new Date(data.startTime).getHours()).forEach(
      hours => distributions[hours]++
  );
  return distributions;
}


function isNil(object) {
  if (object === 0) return false;
  return !object || object.length === 0
}

function showNoDataReminder(data, chartSelector, noDataDivSelector) {
  $(chartSelector).css("display", isNil(data) ? "none" : "block");
  $(noDataDivSelector).css("display", isNil(data) ? "block" : "none");
}

const lineChartToolTipFormat = (xAxisName, yAxisName) => (params) => {
  let data = JSON.parse(params[0].axisValue);
  return `${xAxisName}:${timeStampToDateTimeTranslator(
      params[0].axisValue)}<br/>` +
      `${yAxisName}:${durationcalculate(params[0].value)}<br/>` +
      `Triggered By:${data.triggeredBy}<br/>` +
      `Commit version:${data.lastCommitHash}<br/>`
};

const durationToolTipFormat = (xAxisName, yAxisName) => (params) => {
  let data = JSON.parse(params[0].axisValue);
  return `${xAxisName}:${timeStampToDateTimeTranslator(
      params[0].axisValue)}<br/>` +
      `${yAxisName}:${durationcalculate(params[0].value)}<br/>` +
      `Triggered By:${data.triggeredBy}<br/>` +
      `Commit version:${data.lastCommitHash}<br/>` +
      `Result:${resultCodeMap[data.result]}<br/>` +
      `Num:${data.id}<br/>`;
};

const BUILD_DATA = {
  failureRate: null,
  deploymentFrequency: null,
  buildInfos: []
};
