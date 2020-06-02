Vue.component('opal-chart-panel', {
  props: {
    'items': Array,
    'clazz': String,
  },
  template: `
            <div>
              <opal-chart v-for="item in items" :name="item.name" :key="item.id" :identity="item.id" :option="item.option" :clazz="clazz"/>
            </div>
    `,
});