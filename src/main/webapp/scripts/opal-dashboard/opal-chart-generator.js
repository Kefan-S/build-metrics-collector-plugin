let durationcalculate = function (value) {
  var millisecond = new Number(value);
  var days = parseInt((millisecond / (1000 * 60 * 60 * 24)).toFixed(0));
  var hours = parseInt(
      ((millisecond % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)).toFixed(0));
  var minutes = parseInt(
      ((millisecond % (1000 * 60 * 60)) / (1000 * 60)).toFixed(0));
  var seconds = (millisecond % (1000 * 60)) / 1000;
  var time = ""
  if (days) {
    time = time + days + " 天 ";
  }
  if (hours) {
    time = time + hours + " 小时 ";
  }
  if (minutes) {
    time = time + minutes + " 分 ";
  }
  if (seconds) {
    time = time + seconds.toFixed(2) + " 秒 ";
  }
  return time;
};

let timeStampToDateTranslator = function (value, index) {
  var date = new Date(new Number(value));
  var texts = [(date.getMonth() + 1), date.getDate()];
  if (index === 0) {
    texts.unshift(1900 + date.getYear());
  }
  return texts.join('/');
}

let timeStampToDateTimeTranslator = function (value) {
  var date = new Date(new Number(value));
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
}

function lineChartOptionGenerator(chartName, xAxisData, yAxisData,
    yAxisName, xAxisName = "Start Time") {
  return {
    grid: {
      left: 120
    },
    title: {
      text: chartName
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        animation: false
      },
      formatter: function (params) {
        return xAxisName + ":" + timeStampToDateTimeTranslator(
            params[0].axisValue)
            + "<br/>" + yAxisName + ":" + durationcalculate(params[0].value)
      }
    },
    xAxis: {
      name: xAxisName,
      type: 'category',
      data: xAxisData,
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
      data: yAxisData,
      type: 'line',
      smooth: true
    }]
  }
}

function gagueChartOptionGenerator(chartName, data, formatter, metricsName,
    toolTipFormatter) {
  return {
    title: {
      text: chartName,
    },
    tooltip: {
      formatter: toolTipFormatter
    },
    toolbox: {
      feature: {
        restore: {},
        saveAsImage: {}
      }
    },
    series: [
      {
        name: metricsName,
        type: 'gauge',
        detail: {formatter: formatter},
        data: [{value: data}]
      }
    ]
  }
}

function noDataOptionGeneratior(chartName) {
  return {
    title: {
      show: true,
      textStyle: {
        color: 'grey',
        fontSize: 20
      },
      text: `${chartName}\n\nNo data to display`,
      left: 'center',
      top: 'center'
    },
    xAxis: {
      show: false
    },
    yAxis: {
      show: false
    },
    series: []
  };
}

function invalidOpalDataFilter(xAxisData, yAxisData) {
  let points = yAxisData.filter(data => data !== null).map((data, index) => ({
    xAxis: xAxisData[index],
    yAxis: data
  }));
  return {
    xAxisData: points.map(data => data.xAxis),
    yAxisData: points.map(data => data.yAxis)
  }
}

function isNil(object) {
  if (object === 0) return true;
  return !object || object.length === 0
}

function showNoDataReminder(data, chartSelector, noDataDivSelector) {
  $(chartSelector).css("display", isNil(data) ? "none" : "inline");
  $(noDataDivSelector).css("display", isNil(data) ? "inline" : "none");
}
