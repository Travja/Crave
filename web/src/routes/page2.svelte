<!--<script context="module">-->
<!--    export const prerender = true;-->
<!--</script>-->

<script>

</script>

<svelte:head>
    <title>Travja - Home</title>
</svelte:head>

<section>
    <h1>Welcome to Crave!</h1>
    <img class="construction" src="/construction.svg"/>
    <p>This is page 2</p>
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