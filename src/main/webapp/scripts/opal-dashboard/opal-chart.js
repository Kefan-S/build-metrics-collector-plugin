Vue.component('opal-chart', {
  props:['option', 'identity', 'clazz', 'name', 'clickEvent'],
  template: `<div>
                  <div v-show="option" :id="identity" :class="clazz"></div>
                  <div v-if="!option" class="opal-no-data-div">
                    <div>{{name}}<br/>No data to display</div>
                  </div>
              </div>`,
  mounted() {
    this.$nextTick(function() {
      this.draw()
      window.addEventListener('resize', this.onResize);
    })
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize);
  },
  computed: {
    ...Vuex.mapGetters([
      'skipToJobDetailEvent',
    ])
  },
  methods:{
    draw: function () {
      if (this.option && document.getElementById(this.identity)){
        let chart = echarts.init(document.getElementById(this.identity))
        if(this.clickEvent){
          chart.off('click');
          chart.on("click", this[this.clickEvent]);
        }
        chart.setOption(this.option);
        chart.resize();
      }
    },
    onResize: function () {
      this.draw();
    }
  },
  watch: {
    option(){
      this.$nextTick(() => this.draw())
    }
  }
});