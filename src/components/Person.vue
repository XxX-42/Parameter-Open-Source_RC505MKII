<template>
    <div class="person">
        <h2>一个{{car.brand}},价值{{ car.price }}万</h2>
        <button @click="changePrice">修改汽车的价格</button>
    </div>
  </template>
  
 <script lang="ts" setup name="Person">
    import { reactive } from 'vue';
  //data
  let car = reactive({brand:'texla',price:100})
  //methods
  function changePrice(){
    car.price += 10
    console.log(car.price)
  }
</script>
  
  <style scoped>
    .person {
      background-color: skyblue;
      box-shadow: 0 0 10px;
      border-radius: 10px;
      padding: 20px;
    }
    button {
      margin: 0 5px;
    }
  </style>