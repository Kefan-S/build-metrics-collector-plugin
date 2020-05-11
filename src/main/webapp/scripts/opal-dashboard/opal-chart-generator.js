let durationcalculate = function (value) {
  var millisecond = new Number(value);
  var days = parseInt(millisecond / (1000 * 60 * 60 * 24));
  var hours = parseInt(
      (millisecond % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  var minutes = parseInt((millisecond % (1000 * 60 * 60)) / (1000 * 60));
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
    time = time + seconds.toFixed(0) + " 秒 ";
  }
  return time;
};

let timeStampTranslator = function (value, index) {
  // 格式化成月/日，只在第一个刻度显示年份
  console.warn(value)
  var date = new Date(new Number(value));
  var texts = [(date.getMonth() + 1), date.getDate()];
  if (index === 0) {
    texts.unshift(1900 + date.getYear());
  }
  return texts.join('/');
}

function lineChartOpationGenerator(chartName, xAxisData, yAxisData,
    yAxisName, xAxisName = "Start Time") {
  if (xAxisData && yAxisData) {
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
          return xAxisName + ":" + timeStampTranslator(params[0].axisValue, 0)
              + "<br/>" + yAxisName + ":" + durationcalculate(params[0].value)
        }
      },
      xAxis: {
        name: xAxisName,
        type: 'category',
        data: xAxisData,
        axisLabel: {
          formatter: timeStampTranslator
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
  return noDataOpationGeneratior(chartName);
}

function gagueChartOpationGenerator(chartName, data, formatter) {
  if (data){
    return {
      title: {
        text: chartName
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
  return noDataOpationGeneratior(chartName);
}

function noDataOpationGeneratior(chartName) {
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
