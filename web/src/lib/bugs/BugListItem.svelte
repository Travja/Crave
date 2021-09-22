<svelte:options accessors/>
<script>
    import {createEventDispatcher} from "svelte";
    import StatusIndicator from "$lib/bugs/StatusIndicator.svelte";

    export let status = "unconfirmed";
    export let title = "Untitled";
    export let comments = [];
    export let index;

    const dispatch = createEventDispatcher();
    const clicked = () => {
        dispatch('click', {target: base, index});
    };
    let base;
</script>

<li on:click={clicked} {index} bind:this={base}>
    <div class="title">{title}</div>
    <StatusIndicator {status}/>
    <div class="cmnt">{comments.length} comments</div>
</li>

<style>
    li {
        display: flex;
        align-items: center;
        border-bottom: 1px solid var(--fg-color);
        padding: 20px 10px;
    }

    .transitions-enabled li {
        transition: background-color .2s ease-out;
    }

    li:hover {
        background-color: rgba(0, 0, 0, 0.2);
        cursor: pointer;
    }

    .cmnt {
        color: var(--disabled-fg-color);
    }

    .title {
        flex-grow: 1;
        font-weight: bold;
        font-size: 1.2em;
    }
</style>