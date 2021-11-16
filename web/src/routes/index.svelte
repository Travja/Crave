<!--<script context="module">-->
<!--export const prerender = true;-->
<!--</script>-->

<script>
    import {gateway, title} from "$lib/variables";
    import {onMount} from "svelte";

    let gate;

    title.set("Home");

    onMount(() => gate = gateway());
</script>

<section>
    <h1>Welcome to Crave!</h1>
    <img alt="Under Construction" class="construction" src="/construction.svg"/>
    <p>This site is still under construction.</p>
    <div>Gateway: {gate}</div>
</section>

<style>
    section {
        display: flex;
        flex-direction: column;
        /*justify-content: center;*/
        align-items: center;
        flex: 1;
        flex-grow: 1;
    }

    h1 {
        font-size: 2rem;
    }

    .construction {
        width: 200px;
    }
</style>