<script>
    import '../app.css';
    import Header from "$lib/header/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import {title} from "$lib/variables.js";
</script>

<svelte:head>
    <title>Crave - {$title}</title>
</svelte:head>

<main>
    <Header/>

    <section>
        <slot/>
    </section>
    <Footer/>
</main>

<style>
    main {
        flex: 1;
        display: flex;
        flex-direction: column;
        padding: 0;
        width: 100%;
        max-width: 80%;
        margin: 0 auto;
        box-sizing: border-box;
        box-shadow: 5px 0px 10px #666, -5px 0px 10px #666;
        background-color: white;
    }

    section {
        padding: 1rem;
        flex: 1;
        display: flex;
        flex-direction: column;
        width: 100%;
        margin: 0 auto;
        box-sizing: border-box;
    }
</style>