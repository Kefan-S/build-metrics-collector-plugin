const store = new Vuex.Store({
  state: {
    users: [],
    jobs: [],
    dateRange: [new Date(Date.now() - 1000 * 60 * 60 * 24 * 14), new Date()],
    selectedJob: '',
    selectedUser: '',
    data: null,
  },
  getters: {
    skipToJobDetailEvent: ({selectedJob}) => {
        return (param) => {
          let data = JSON.parse(param.name);
          window.open(window.location.href.replace(/\/job.*(?=\/opal)/g,'').replace("opal", "job/") + selectedJob + "/" + data.id);
        }
    },
    lineChartItems: ({data}) => {
      data = data ? data : BUILD_DATA;
      let recoveryTimeData = data.buildInfos.filter(({recoveryTime}) => recoveryTime !== null);
      let leadTimeData = data.buildInfos.filter(({leadTime})  => leadTime !== null);
      let durationData = data.buildInfos.filter(({duration}) => duration !== null);
      return [{
        id:"opal-recovery-time-chart",
        name: "Recovery Time",
        option:lineChartOptionGenerator('Recovery Time', recoveryTimeData, "Recovery Time", "End Time", "recoveryTime", lineChartToolTipFormat("End Time", "Recovery Time"))
      },{
        id:"opal-lead-time-chart",
        name: "Lead Time",
        option: lineChartOptionGenerator('Lead Time', leadTimeData, "Lead Time", "End Time", "leadTime", lineChartToolTipFormat("End Time", "Lead Time"))
      },{
        id:"opal-duration-chart",
        name: "Duration",
        option: lineChartOptionGenerator('Duration', durationData, "Duration", "End Time", "duration", durationToolTipFormat("End Time", "Duration")),
        clickEvent: 'skipToJobDetailEvent'
      },{
        id:"opal-build-distribution-chart",
        name: "Build Distribution",
        option:deployTimeDistributionChartOptionGenerator(data)
      }]
    },
    gaugeChartItems: ({data}) => {
      data = data ? data : BUILD_DATA;
      data.failureRate = data.failureRate ? (data.failureRate * 100).toFixed(2) : data.failureRate;
      return [{
        id:"failure-rate-chart",
        name: "Failure Rate",
        option: gaugeChartOptionGenerator("Failure Rate", data.failureRate, "{value}%", 'Failure Rate', '{a} <br/>{b} : {c}%',
            [[0.1, '#91c7ae'], [0.3, '#FFA500'], [0.5, '#c23531'], [1, '#990077']], 100, value => value)
      },{
        id:"deploy-frequency-chart",
        name: "Deploy Frequency",
        option: gaugeChartOptionGenerator("Deploy Frequency", data.deploymentFrequency, "{value}", 'Deploy Frequency', '{a} <br/>{b} : {c}',
            [[0.1, '#c23531'], [0.8, '#63869e'], [1, '#91c7ae']], 200, value => value === 200 ? value + "+" : value)
      }]
    }
  },
  mutations: {
    'GET_USERS_SUCCESS': (state, payload) => {
      state.users = ['All users', ...payload];
      state.selectedUser = 'All users'
    },
    'GET_JOBS_SUCCESS': (state, payload) => {
      state.jobs = payload;
      state.selectedJob = payload[0]
    },
    'GET_DATA_SUCCESS': (state, playload) => state.data = playload,
    'UPDATE_SELECTED_USERS': (state, playload) => {
      state.users = playload;
      state.selectedUser = playload[0];
    },
    'UPDATE_DATE_RANGE': (state, playload) => {
      state.dateRange = playload;
    },
    'UPDATE_SELECTED_USER': (state, playload) => {
      state.selectedUser = playload;
    },
    'UPDATE_SELECTED_JOB': (state, playload) => {
      state.selectedJob = playload;
    },
    "UPDATE_DATE" : (state) => {
      let offset = state.dateRange[1] - state.dateRange[0];
      state.dateRange = [new Date(Date.now() - offset), new Date()]
    }
  },
  actions: {
    'GET_USERS': ({commit, state}) => {
      let param = {
        jobName: state.selectedJob,
      };
      $.get(`${window.location.href.replace(/\/job.*(?=\/opal)/g,'')}/users`, param, function (data, status) {
        if (status === 'success') {
          commit("GET_USERS_SUCCESS", data);
        }
      })
    },
    'GET_JOBS': ({commit}) => {
      $.get(`${window.location.href.replace(/\/job.*(?=\/opal)/g,'')}/jobs`, function (data, status) {
        if (status === 'success') {
          commit("GET_JOBS_SUCCESS", data);
        }
      })
    },
    'GET_DATA': ({commit, state}) => {
      let param = {
        jobName: state.selectedJob,
        beginTime: new Date(state.dateRange[0].toLocaleDateString()).getTime(),
        endTime: new Date(state.dateRange[1].toLocaleDateString()).getTime() + 24 * 60 * 60 * 1000 - 1,
        triggerBy: state.selectedUser
      };
      $.get(`${window.location.href.replace(/\/job.*(?=\/opal)/g,'')}/data`, param, function (data, status) {
        if (status === 'success') {
          commit("GET_DATA_SUCCESS", data);
        }
      })
    },
    'UPDATE_DATE': ({commit}) => {
      setInterval(() => commit('UPDATE_DATE'), 60 * 60 * 1000);
    }
  }
});