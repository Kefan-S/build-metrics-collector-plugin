Vue.component('opal-user-selector', {
  template: `
          <div class="opal-filter-container">
            <span class="opal-selector-title">Trigger By</span>
            <i-select :value='selectedUser' style="width:150px; height: 33px" placeholder="select user" @on-change="userChange">
              <i-option v-for="user in users" :key="user" :value="user">{{user}}</i-option>
            </i-select>
          </div>
    `,
  methods: {
    ...Vuex.mapActions({
      getUsers: 'GET_USERS',
      getData: 'GET_DATA'
    }),
    ...Vuex.mapMutations({
      updateSelectedUser: 'UPDATE_SELECTED_USER'
    }),
    userChange(newVal){
      this.updateSelectedUser(newVal);
    }
  },
  computed: Vuex.mapState([
    'users',
    'selectedJob',
    'selectedUser'
  ]),
  watch: {
    selectedJob(){
      this.getUsers()
    },
    selectedUser() {
      this.getData();
    }
  }

});