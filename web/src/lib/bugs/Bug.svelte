<svelte:options accessors/>
<script>
    import CloseButton from '$lib/ui/CloseButton.svelte';
    import {createEventDispatcher} from 'svelte';
    import {fly, fade} from 'svelte/transition';
    import StatusIndicator from "$lib/bugs/StatusIndicator.svelte";

    export let status = "Unconfirmed";
    export let title = "Untitled";
    export let description = "No description.";
    export let comments = [];
    export const key = {};

    const dispatch = createEventDispatcher();

    let desc;
    let isFocused = false;

    const editDescription = () => {
        desc.contentEditable = true;
        desc.focus();
        isFocused = true;
    };

    const loseFocus = (e) => {
        desc.contentEditable = false;
        isFocused = false;
        updated(description);
    };

    const updated = (e) => {
        dispatch('updated', {description});
    };

    export const addComment = (comment) => {
        comments = [...comments, comment];
    };

    let block;
    export const addEventListener = (evt, callback) => {
        block.addEventListener(evt, callback);
    };

    const closeClicked = () => {
        dispatch('close', {});
    };

</script>

<div class="modal" transition:fade>
<div class="bug"
     bind:this={block}
     in:fly={{ x: -200, duration: 500 }} out:fly={{ x: -200, duration: 250 }}>
    <CloseButton on:click={closeClicked}/>
    <div class="content">
        <div class="">
            <h3 class="centered">{title}</h3>
            <h4>Status: <StatusIndicator {status}/></h4>
            <hr>
            <div class="desc">
                <div class="edit" on:click={editDescription}>Edit</div>
                <div on:blur={loseFocus}
                     contenteditable="false"
                     bind:this={desc}
                     bind:textContent={description}></div>
            </div>
            <hr>
        </div>
        {#if comments && comments.length > 0}
            <h4>Comments</h4>
            {#each comments as comment}
                <div>{comment}</div>
            {/each}
        {:else}
            <div class="comment">
                <div>No comments... Be the first!</div>
            </div>
        {/if}
    </div>
</div>
</div>

<style>
    .modal {
        display: flex;
        align-items: center;
        justify-content: center;
        position: fixed; /* Stay in place */
        z-index: 1; /* Sit on top */
        left: 0;
        top: 0;
        width: 100%; /* Full width */
        height: 100%; /* Full height */
        overflow: auto; /* Enable scroll if needed */
        background-color: rgb(0,0,0); /* Fallback color */
        background-color: rgba(0,0,0,0.4); /* Black w/ opacity */
    }

    .bug {
        position: relative;
        margin: 20px auto;
        padding: 20px;
        width: 70%;
        background-color: var(--bg-color);
        border-radius: 10px;
        box-shadow: 5px 5px 10px #000;
    }

    .content {
        position: relative;
        display: flex;
        flex-direction: column;
    }

    h3 {
        margin: 0;
    }

    .centered {
        text-align: center;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .desc {
        position: relative;
    }

    hr {
        margin: 12px 0;
        border-color: var(--fg-color);
        transition: border;
        transition-duration: .3s;
        transition-timing-function: ease-in-out;
    }

    .edit {
        float: right;
        /*position: absolute;*/
        /*top: 0;*/
        /*right: 20px;*/
        color: var(--link-color);
        text-decoration: underline;

        transition-property: color, text-shadow, background-color;
        transition-duration: .3s;
        transition-timing-function: ease-in-out;
    }

    .edit:hover {
        cursor: pointer;
    }

    .comment {
        display: flex;
        flex-grow: 1;
        align-items: flex-end;
    }
</style>