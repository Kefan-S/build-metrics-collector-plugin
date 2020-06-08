Vue.component('opal-chart-panel', {
  props: {
    'items': Array,
    'clazz': String,
    'span': String,
  },
  template: `
            <i-col :span="span">
              <opal-chart v-for="item in items" :name="item.name" :key="item.id" :clickEvent="item.clickEvent" :identity="item.id" :option="item.option" :clazz="clazz"/>
            </i-col>
    `,
});