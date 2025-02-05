<template>
    <div class="app">
        <h1>父组件</h1>
        <Person>ffff</Person>
    </div>
</template>

<script>
import Person from './components/Person.vue'

export default {
    name: 'App',
    components: { Person }
};
</script>

<style scoped>
.app {
    background-color: brown;
    box-shadow: 0 0 10px;
    padding: 20px;
}
</style>