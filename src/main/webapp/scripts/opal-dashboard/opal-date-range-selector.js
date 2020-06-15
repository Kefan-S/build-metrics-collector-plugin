Vue.component('opal-date-range-selector', {
  template: `
      <div class="opal-date-range-container">
      <div class="opal-selector-title">Date Range</div>
      <i-col span="12">
        <Date-picker :value="dateRange" @on-change="dateRangeChange" type="daterange" placement="bottom-end" :clearable="false" placeholder="select date" class="opal-date-range">
        </Date-picker>
      </i-col>
      </div>
    `,
  computed: Vuex.mapState([
    'dateRange'
  ]),
  mounted() {
    this.updateDate();
  },
  methods: {
    ...Vuex.mapActions({
      getData: 'GET_DATA',
      updateDate: 'UPDATE_DATE',
    }),
    ...Vuex.mapMutations({
      updateDateRange: 'UPDATE_DATE_RANGE'
    }),
    dateRangeChange(newVal){
      this.updateDateRange(
          newVal.map(date => new Date(date).getTime())
                .map(timestamp =>  timestamp + new Date().getTimezoneOffset() * 60 * 1000)
                .map(timestamp => new Date(timestamp))
      );
      this.getData();
    }
  },
});