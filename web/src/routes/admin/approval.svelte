<script>
    import {gateway, title} from "$lib/variables";
    import {onMount} from "svelte";
    import PendingSaleItem from "$lib/pending/PendingSaleItem.svelte";
    import PendingItem from "$lib/pending/PendingItem.svelte";

    title.set("Item Approval");

    let fetchPending = true, fetchingSales = true;
    let needsApproval = [],
        saleApprovals = [];

    const getPendingItems = () => {
        fetchPending = true;
        fetch(gateway() + "/item-service/pending")
            .then(res => res.json())
            .then(data => {
                console.log(data);
                needsApproval = data;
                fetchPending = false;
            })
            .catch(e => {
                console.error(e);
                fetchPending = false;
            });
    };

    const approveItem = id => {
        fetch(`${gateway()}/item-service/pending/approve/${id}`, {method: "POST"})
            .then(res => {
                console.log(res);
                if (res.status == 204) {
                    getPendingItems();
                }
            })
            .catch(e => {
                console.error(e);
            });
    };

    const getPendingSales = () => {
        fetchingSales = true;
        fetch(gateway() + "/sale-service/sale/pending")
            .then(res => res.json())
            .then(data => {
                console.log(data);
                saleApprovals = data;
                fetchingSales = false;
            })
            .catch(e => {
                console.error(e);
                fetchingSales = false;
            });
    };

    onMount(() => {
        getPendingItems();
        getPendingSales();
    });
</script>

<h3>Pending Items ({needsApproval.length})</h3>
{#if fetchPending}
    <p>Fetching pending items...</p>
{:else}
    {#if needsApproval.length > 0}
        {#each needsApproval as item}
            <PendingItem {...item} on:approved={getPendingItems} on:rejected={getPendingItems}/>
        {/each}
    {:else}
        <p>There are no pending items at the moment! Good work!</p>
    {/if}
{/if}

<h3>Pending Sales ({saleApprovals.length})</h3>
{#if fetchingSales}
    <p>Fetching pending sales...</p>
{:else}
    {#if saleApprovals.length > 0}
        {#each saleApprovals as sale}
            <PendingSaleItem {...sale} on:approved={getPendingSales} on:rejected={getPendingSales}/>
        {/each}
    {:else}
        <p>There are no pending items at the moment! Good work!</p>
    {/if}
{/if}

<style>
    .item {
        display: flex;
        flex-direction: row;
        align-items: center;
        padding: 0.5rem;
    }

    .item:nth-child(2n-1) {
        background: var(--bg-color-secondary);
    }

    .filler {
        flex-grow: 1;
    }

    .approve {
        background: green;
        color: white;
    }
</style>