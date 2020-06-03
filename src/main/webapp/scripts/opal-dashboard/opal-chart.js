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
      echarts.init(document.getElementById(this.identity));
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
    ]),
    chart() {
      return echarts.getInstanceByDom(document.getElementById(this.identity))
    }
  },
  methods:{
    draw() {
      if (this.option){
        if(this.clickEvent){
          this.chart.off('click');
          this.chart.on("click", this[this.clickEvent]);
        }
        this.chart.setOption(this.option);
        this.chart.resize();
      }
    },
    onResize: function () {
      this.chart.resize();
    }
  },
  watch: {
    option(){
      this.$nextTick(() => this.draw())
    }
  }
});