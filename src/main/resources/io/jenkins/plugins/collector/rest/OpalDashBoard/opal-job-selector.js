Vue.component('opal-job-selector', {
  template: `
          <div>
            <span class="opal-selector-title">Monitored Job</span>
            <i-select :value="selectedJob" class="opal-selector" placeholder="select job" @on-change="jobChange">
              <i-option v-for="job in jobs" :key="job" :value="job">{{job}}</i-option>
            </i-select>
          </div>
    `,
  computed: Vuex.mapState([
    'jobs',
    'selectedJob'
  ]),
  methods: {
    ...Vuex.mapActions({
      getData: 'GET_DATA',
      getJobs: 'GET_JOBS'
    }),
    ...Vuex.mapMutations({
      updateSelectedJob: 'UPDATE_SELECTED_JOB'
    }),
    jobChange(newVal){
      this.updateSelectedJob(newVal);
      this.getData();
    }
  },
  mounted() {
    this.$nextTick(function () {
      this.getJobs();
    })
  }
});