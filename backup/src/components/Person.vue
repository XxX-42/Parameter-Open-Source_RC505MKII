<template>
    <div class="person">
        <h1>hello</h1>
        <h2>年龄:{{a}}</h2>
        <h2>年龄:{{b}}</h2>
        <button @click="showTel">查看联系方式</button>
        <button @click="changeName">修改名字</button>
        <button @click="changeAge">修改年龄</button>
    </div>
</template>

<script>
    export default {
        name: 'Person',
        setup(){
            //数据
            let name = '张三哥'
            let age = 18
            let tel = 'ssssssss'
            
            //方法
            function changeName(){
                name = 'nobody'
            }
            function changeAge(){
                age += 1
            }
            function changeName(){
                name = 'nobody'
            }

            return{a:name,b:age,changeAge}
        }
};
</script>

<style scoped>
.person {
    background-color: aqua;
    box-shadow: 0 0 10px;
    padding: 20px;
}
</style>