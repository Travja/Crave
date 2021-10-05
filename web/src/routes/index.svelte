<!--<script context="module">-->
<!--export const prerender = true;-->
<!--</script>-->

<script>
    import {title, variables} from "$lib/variables";
    import {onMount} from "svelte";

    let gateway = "...";
    title.set("Home");

    onMount(() => {
        gateway = variables.gateway ? variables.gateway : window.location.origin;
    });
</script>

<section>
    <h1>Welcome to Crave!</h1>
    <img class="construction" src="/construction.svg" alt="Under Construction"/>
    <p>This site is still under construction.</p>
    <div>Gateway: {gateway}</div>
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