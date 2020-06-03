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
    })
  },
  computed: {
    ...Vuex.mapGetters([
      'skipToJobDetailEvent',
    ])
  },
  methods:{
    draw() {
      if (this.option){
        let chart = echarts. getInstanceByDom(document.getElementById(this.identity))
        if(this.clickEvent){
          chart.off('click');
          chart.on("click", this[this.clickEvent]);
        }
        chart.setOption(this.option);
        chart.resize();
      }
    }
  },
  watch: {
    option(){
      this.$nextTick(() => this.draw())
    }
  }
});