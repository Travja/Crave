<!--<script context="module">-->
<!--    export const prerender = true;-->
<!--</script>-->
<script>
    import {title, variables} from "$lib/variables";
    import {onDestroy, onMount} from "svelte";
    import PageItem from "$lib/PageItem.svelte";
    import {fly} from 'svelte/transition';

    title.set("Items");

    let tip = true;
    let gateway;
    let items, error;
    let inProgress = false;

    const getItems = async () => {
        if (!gateway)
            return;

        error = undefined;
        inProgress = true;
        const res = await fetch(gateway + '/item-service/items');
        inProgress = false;
        if (res.ok) {
            items = await res.json();
            console.log("Items: ", items);
            return;
        }

        error = await res.json();
        // console.error("Could not fetch items.");
        // console.error(error);
        switch (error.status) {
            case 401:
                error = "User not authenticated.";
                break;
            case 403:
                error = "User is not allowed to access this resource";
                break;
            case 503:
                error = "Services unavailable";
                break;
            default:
                error = "An unknown error occurred.";
        }
    };

    onMount(async () => {
        gateway = variables.gateway ? variables.gateway : window.location.origin;

        await getItems();
    });

    let unsubscribe = variables.jwt.subscribe(getItems);
    onDestroy(unsubscribe);

</script>
<section>
    <h1>Directory</h1>
    <div id="intro">
        <p>Finding the lowest prices has never been so easy!</p>
    </div>
    <hr>
    {#if items}
        <div id="items">
            {#each items as item, i}
                {#if item.details}
                    <PageItem {...item}/>
                    <!--{#if i < items.length - 1}-->
                    <!--    <div class="spacer"/>-->
                    <!--{/if}-->
                {/if}
            {/each}
            {#each Array(8) as i}
                <div class="fix"/>
            {/each}
        </div>
    {:else if inProgress}
        <p class="no-items">Fetching items..</p>
    {:else if error}
        <p class="no-items">Could not fetch items. {error}</p>
    {:else}
        <p class="no-items">No items available.</p>
    {/if}
</section>
{#if tip}
    <div id="tip"
         on:click={(e) => tip = false}
         out:fly="{{ x: 200, duration: tip == false ? 250 : 0}}">
        <div>Can't find what you're looking for? Make sure to add it!</div>
        <a alt="Scan a Receipt!" class="button" href="/scan" id="scan">Scan a Receipt!</a>
    </div>
{/if}
<style>
    section {
        display: flex;
        flex-direction: column;
        /*justify-content: center;*/
        /*align-items: center;*/
        flex: 1;
        flex-grow: 1;
    }

    h1 {
        font-size: 2rem;
        margin: 0;
    }

    #intro {
        display: flex;
        align-items: center;
    }

    #items {
        position: relative;
        display: flex;
        flex-wrap: wrap;
        background: var(--bg-color);
    }

    .fix {
        display: flex;
        flex: auto;
        background: transparent;
        padding: 0px 21px;
        flex-grow: 1;
    }

    .fix::after {
        content: "";
        min-width: 200px;
        max-width: 200px;
    }

    .no-items {
        color: #666;
        margin-left: 10px;
    }

    hr {
        width: 30%;
        margin: 0.5em 0;
    }

    #tip {
        position: fixed;
        bottom: 1em;
        right: 1em;
        background: #ddd;
        border-radius: 1em;
        padding: 1em;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        box-shadow: 0 0 15px #333;
        z-index: 1000;
    }
</style>