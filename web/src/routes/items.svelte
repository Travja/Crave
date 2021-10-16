<!--<script context="module">-->
<!--    export const prerender = true;-->
<!--</script>-->
<script>
    import {title, variables} from "$lib/variables";
    import {onDestroy, onMount} from "svelte";
    import PageItem from "$lib/PageItem.svelte";
    import {overrideFetch} from "$lib/util";

    title.set("Items");

    let gateway;
    let items, error;
    let inProgress = false;

    const getItems = async () => {
        if (!gateway)
            return;

        error = undefined;
        inProgress = true;
        const res = await fetch(gateway + '/item-service/item-details');
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
            default:
                error = "An unknown error occurred.";
        }
    };

    onMount(async () => {
        gateway = variables.gateway ? variables.gateway : window.location.origin;

        overrideFetch();

        await getItems();
    });

    let unsubscribe = variables.jwt.subscribe(getItems);
    onDestroy(unsubscribe);

</script>
<section>
    <h1>Directory</h1>
    <p>Finding the lowest prices has never been so easy!</p>
    <hr>
    {#if items}
        <div id="items">
            {#each items as item}
                <PageItem {...item}/>
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
        margin-bottom: 0;
    }

    #items {
        position: relative;
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        justify-content: center;
        align-items: center;
    }

    .no-items {
        color: #666;
        margin-left: 10px;
    }

    hr {
        width: 30%;
        margin: 0.5em 0;
    }
</style>