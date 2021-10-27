<!--<script context="module">-->
<!--export const prerender = true;-->
<!--</script>-->

<script>
    import {title, gateway} from "$lib/variables";

    title.set("Home");
</script>

<section>
    <h1>Welcome to Crave!</h1>
    <img class="construction" src="/construction.svg" alt="Under Construction"/>
    <p>This site is still under construction.</p>
    <div>Gateway: {gateway()}</div>
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