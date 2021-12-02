<script>
    import '../app.css';
    import Header from "$lib/header/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    // noinspection ES6UnusedImports
    import {title} from "$lib/variables.js";
    import {initScroll, overrideFetch, overrideXMLSend, scrollDistance, setupButtons} from "$lib/util";
    import {afterUpdate, beforeUpdate, onDestroy, onMount} from "svelte";
    import Parrallax from "$lib/Parrallax.svelte";

    let unsubscribe, unscroll;
    let firstRun = true;

    onMount(() => {
        unscroll = scrollDistance.subscribe(value => {
            let bg = document.getElementById("bg-parrallax");
            if (bg)
                bg.style.backgroundPosition = `50% ${(50 - value * 2)}%`;
        });
        initScroll();
    });

    onDestroy(() => {
        if (unsubscribe)
            unsubscribe();
        if (unscroll)
            unscroll();
    });

    beforeUpdate(() => {
        overrideFetch();
        overrideXMLSend();
    });

    afterUpdate(() => {
        setupButtons();
    });
</script>

<svelte:head>
    <title>Crave - {$title}</title>
</svelte:head>

<main>
    <Parrallax height="10em"><h1>{$title}</h1></Parrallax>
    <Header/>
    <section class="content">
        <slot/>
    </section>
    <Footer/>
</main>
<!--<div style="height: 15000px"/>-->

<style>
    main {
        background: var(--bg-color);
    }

    .content {
        position: relative;
        padding: 0.7rem;
        margin-top: -0.6rem;
        position: relative;
        z-index: 3;
        background: var(--bg-color);
    }

    @media only screen and (min-width: 768px) {
        main {
            flex: 1;
            display: -webkit-flex;
            display: flex;
            flex-direction: column;
            padding: 0;
            width: 80%;
            margin: 0 auto;
            box-sizing: border-box;
            box-shadow: 5px 0px 10px #666, -5px 0px 10px #666;
            transform-style: inherit;
            perspective: inherit;
            perspective-origin: inherit;
        }

        .content {
            position: relative;
            padding: 1rem;
            flex: 1;
            display: -webkit-flex;
            display: flex;
            flex-direction: column;
            width: 100%;
            margin: -0.6rem auto 0;
            box-sizing: border-box;
            background: inherit;
        }
    }
</style>