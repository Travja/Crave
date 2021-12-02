<!--<script context="module">-->
<!--export const prerender = true;-->
<!--</script>-->

<!--suppress CssUnresolvedCustomProperty -->
<script>
    import {onDestroy, onMount} from "svelte";
    import {scrollDistance} from "$lib/util";

    export let src = "/groceries.jpg";

    let unsubscribe;
    let content, bg;

    onMount(() => {
        unsubscribe = scrollDistance.subscribe(value => {
            if (bg)
                bg.style.backgroundPosition = `50% ${(value * 2)}%`;
        });
    });

    onDestroy(() => {
        if (unsubscribe)
            unsubscribe();
    });
</script>

<div class="main" style="--src: url({src})">
    <div bind:this={content} class="content">
        <slot/>
    </div>
    <div bind:this={bg} class="bg"></div>
</div>

<style>
    .main {
        display: none;
        position: relative;
        align-items: center;
        justify-content: center;
        background-color: white;
        max-width: 100%;
        max-height: 10em;
    }

    @media only screen and (min-width: 768px) {
        .main {
            display: flex;
            position: sticky;
            position: -webkit-sticky;
            top: 0;
            left: 0;
            max-height: 10em;
        }

        .content {
            height: 10em;
        }
    }

    .bg {
        position: absolute;
        top: 0;
        left: 0;
        /*background-attachment: fixed;*/
        background-size: 100vw auto;
        /*background-position: center;*/
        background-image: var(--src);
        width: 100%;
        height: 100%;
        z-index: 0;
        box-shadow: inset 0 0 10px 3px #333;
    }

    .content {
        color: white;
        text-shadow: 2px 2px 4px #333;
        width: 100%;
        z-index: 2;
        display: flex;
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(4px);
    }
</style>