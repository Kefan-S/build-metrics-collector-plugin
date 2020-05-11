let durationcalculate = function (value) {
  var millisecond = new Number(value);
  var days = parseInt(millisecond / (1000 * 60 * 60 * 24));
  var hours = parseInt((millisecond % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  var minutes = parseInt((millisecond % (1000 * 60 * 60)) / (1000 * 60));
  var seconds = (millisecond % (1000 * 60)) / 1000;
  var time = ""
  if(days) time = time + days + " 天 ";
  if(hours) time = time + hours + " 小时 ";
  if(minutes) time = time + minutes + " 分 ";
  if(seconds) time = time + seconds + " 秒 ";
  return time;
};

let timeStampTranslator = {
  formatter: function (value, index) {
    // 格式化成月/日，只在第一个刻度显示年份
    console.warn(value)
    var date = new Date(new Number(value) * 1000);
    var texts = [(date.getMonth() + 1), date.getDate()];
    if (index === 0) {
      texts.unshift(1900 + date.getYear());
    }
    return texts.join('/');
  }
};

function lineChartOpationGenerator(chartName, xAxisData, yAxisData,
    yAxisName , xAxisName = "Start Time") {
  return {
    title: {
      text: chartName
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        animation: false
      },
      formatter: function (params) {
        return 'X: ' + params.data[0].toFixed(2) + '<br>Y: ' + params.data[1].toFixed(2);
      }
    },
    xAxis: {
      name: xAxisName,
      type: 'category',
      data: xAxisData,
      axisLabel: timeStampTranslator
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

function gagueChartOpationGenerator(chartName, data, formatter) {
  return {
    title: {
      text: 'chartName'
    },
    tooltip: {
      formatter: '{a} <br/>{b} : {c}%'
    },
    toolbox: {
      feature: {
        restore: {},
        saveAsImage: {}
      }
    },
    series: [
      {
        name: '业务指标',
        type: 'gauge',
        detail: {formatter: formatter},
        data: [{value: data, name: '失败率'}]
      }
    ]
  }
}
