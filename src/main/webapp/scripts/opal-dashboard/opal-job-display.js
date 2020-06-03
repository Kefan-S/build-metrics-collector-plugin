Vue.component('opal-job-display', {
  template: `
      <div class="opal-filter-container">
        <div class="opal-selector-title">Monitored Job</div>
        <div class="opal-display-value">{{selectedJob}}</div>
      </div>
  `,
  computed: Vuex.mapState([
      'selectedJob',
  ])
})