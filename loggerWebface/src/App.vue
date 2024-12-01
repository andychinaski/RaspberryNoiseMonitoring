<template>
  <div id="app">
    <header>
      <h1>Мониторинг уровня шума</h1>
    </header>
    <main>
      <p>Добро пожаловать! Здесь вы можете наблюдать за уровнем шума и его историей.</p>
    </main>
    <footer>
      <p>&copy; 2024 Noise Monitoring</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: "App",
};
</script>

<style>
#app {
  text-align: center;
  font-family: Arial, sans-serif;
  margin: 0;
  padding: 0;
}

header {
  background-color: #4caf50;
  color: white;
  padding: 10px;
}

main {
  padding: 20px;
}

footer {
  background-color: #f1f1f1;
  color: #333;
  padding: 10px;
  position: fixed;
  bottom: 0;
  width: 100%;
}
</style>
