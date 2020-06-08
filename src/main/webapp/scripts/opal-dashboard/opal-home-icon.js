Vue.component('opal-home-icon', {
  template: `
          <div class="opal-home-page-icon" title="Home Page" @click="goHome()">
            <Icon type="md-home" size="32"/>
          </div>
    `,
  methods: {
    goHome: () => {
      window.location.replace(window.location.href.replace("/opal", ""));
    }
  }
});