<script setup>
import TopBar from '../components/TopBar.vue'
</script>

<template>
	<div class="min-h-screen bg-[#f7f8f3]">
		<TopBar />
		<main>
			<RouterView />
		</main>
	</div>
</template>
