<svelte:options accessors/>
<script>
    export let title = "Untitled";
    export let description = "No description.";
    export let comments = [];
    export let src = "https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500";

    let desc;
    let isFocused = false;

    const editDescription = () => {
        desc.contentEditable = true;
        desc.focus();
        isFocused = true;
    };

    const loseFocus = () => {
        desc.contentEditable = false;
        console.log(description);
        isFocused = false;
    };

    export const addComment = (comment) => {
        comments = [...comments, comment];
    };

</script>

<div class="bug">
    <div class="centered">
        <div class="img-wrapper">
            <img {src}/>
        </div>
        <h3>{title}</h3>
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

<style>
    img {
        max-width: 100px;
        max-height: 100px;
        box-shadow: 3px 3px 5px #000;
        box-sizing: border-box;
    }

    .img-wrapper {
        height: 100px;
        display: flex;
        justify-content: center;
        align-items: center;
        margin: 10px 0px;
        box-sizing: border-box;
    }

    .bug {
        position: relative;
        display: flex;
        flex-direction: column;
        margin: 20px;
        padding: 20px;
        min-width: 200px;
        max-width: 500px;
        background-color: rgba(0, 0, 0, 0.2);
        border-radius: 10px;
        box-shadow: 5px 5px 10px #000;
    }

    h3 {
        margin: 0;
    }

    .centered {
        text-align: center;
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
        justify-content: center;
    }
</style>