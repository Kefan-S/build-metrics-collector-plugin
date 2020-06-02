Vue.component('opal-chart', {
  props:['option', 'identity', 'clazz', 'name'],
  template: `<div>
                  <div v-show="option" :id="identity" :class="clazz"></div>
                  <div v-if="!option" class="opal-no-data-div">
                    <div>{{name}}<br/>No data to display</div>
                  </div>
              </div>`,
  mounted() {
    this.$nextTick(function() {
      this.draw()
    })
  },
  methods:{
    draw: function () {
      if (this.option && document.getElementById(this.identity)){
        let chart = echarts.init(document.getElementById(this.identity))
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