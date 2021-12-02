<script>
    import '../app.css';
    import Header from "$lib/header/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    // noinspection ES6UnusedImports
    import {title, variables} from "$lib/variables.js";
    import {initScroll, overrideFetch, overrideXMLSend, scrollDistance, setupButtons} from "$lib/util";
    import {afterUpdate, beforeUpdate, onDestroy, onMount} from "svelte";
    import Parrallax from "$lib/Parrallax.svelte";
    import {fly} from "svelte/transition";

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
    {#if variables.tip}
        <div id="tip"
             on:click={(e) => variables.tip = false}
             out:fly="{{ x: 200, duration: variables.tip == false ? 250 : 0}}">
            <div>Can't find what you're looking for? Make sure to add it!</div>
            <a alt="Scan a Receipt!" class="button" href="/scan" id="scan">Scan a Receipt!</a>
        </div>
    {/if}
</main>
<!--<div style="height: 15000px"/>-->

<style>
    main {
        background: var(--bg-color);
        display: flex;
        flex-direction: column;
        flex-grow: 1;
    }

    .content {
        /*position: relative;*/
        padding: 0.7rem;
        margin-top: -0.6rem;
        z-index: 3;
        background: var(--bg-color);
        display: flex;
        flex: 1;
        flex-direction: column;
        transform: translate3d(0.00001px, 0.000001px, 0.00001px);
    }

    #tip {
        position: fixed;
        bottom: 1em;
        right: 1em;
        left: 1em;
        background: #ddd;
        border-radius: 1em;
        padding: 1em;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        box-shadow: 0 0 15px #333;
        z-index: 1000;
        transform: translate3d(0.00001px, 0.000001px, 0.00001px);
    }

    @media only screen and (min-width: 768px) {
        main {
            padding: 0;
            width: 80%;
            margin: 0 auto;
            box-sizing: border-box;
            box-shadow: 5px 0px 10px #666, -5px 0px 10px #666;
            /*transform-style: inherit;*/
            /*perspective: inherit;*/
            /*perspective-origin: inherit;*/
        }

        .content {
            padding: 1rem;
            width: 100%;
            margin: 0 auto;
            box-sizing: border-box;
            background: inherit;
        }

        #tip {
            left: unset;
        }
    }
</style>