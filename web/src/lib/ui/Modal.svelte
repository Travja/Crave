<script>
    import {modal} from "$lib/variables.js";
    import {fly} from "svelte/transition";
    import {onMount} from "svelte";

    let mod;

    const keydown = e => {
        console.log(e.key);
        e.stopPropagation();
        if (e.key === 'Escape') {
            modal.set(undefined);
        }
    };

    const click = e => {
        console.log("Clicked");
        modal.set(undefined);
    };

    onMount(() => {
        if (mod)
            mod.focus();
    });

</script>

<div class="modal" bind:this={mod} on:keydown={keydown} tabindex="{0}" on:click={click} autofocus>
    <div class="backdrop"/>
    <div transition:fly={{y: -50}} class="content-wrapper">
        <slot name="header"/>
        <div class="content">
            <slot/>
        </div>
        <slot name="footer"/>
    </div>
</div>

<style>
    div.modal {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100vh;
        z-index: 100;

        display: flex;
        justify-content: center;
        align-items: center;
    }

    div.backdrop {
        position: absolute;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.4);
    }

    div.content-wrapper {
        z-index: 10;
        /*max-width: 70vw;*/
        border-radius: 0.3rem;
        background-color: white;
        overflow: hidden;
        padding: 1.5em;
        margin: 1em;
    }

    div.content {
        /*max-height: 70vh;*/
        overflow: auto;
    }
</style>