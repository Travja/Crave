<script>
    import {createEventDispatcher, onMount} from "svelte";
    import {gateway} from "$lib/variables";
    import {formatter} from "$lib/util";

    const dispatch = createEventDispatcher();

    export let carried = true,
        inStock = true,
        id,
        item,
        lastUpdated,
        lowestPrice,
        originalPrice,
        price,
        store;

    let updated;
    const dateOpts = {weekday: 'long', year: 'numeric', month: 'short', day: 'numeric'};

    onMount(() => {
        updated = new Date(lastUpdated);
    })

    const approve = () => {
        fetch(`${gateway()}/item-service/pending/approve/${id}`, {method: "POST"})
            .then(res => {
                console.log(res);
                if (res.status == 204) {
                    dispatch("approved");
                }
            })
            .catch(e => {
                console.error(e);
            });
    };

    const reject = () => {
        fetch(`${gateway()}/item-service/pending/reject/${id}`, {method: "POST"})
            .then(res => {
                console.log(res);
                if (res.status == 204) {
                    dispatch('rejected')
                }
            })
            .catch(e => {
                console.error(e);
            });
    };

</script>

<div class="item">
    <div class="line-one">
        <span>
            <h4 class="wrapper">
                {item.name}
            </h4>
            <div class="dates">
                Posted: {updated?.toLocaleDateString('en-us', dateOpts)}
            </div>
        </span>
        <div class="filler"/>
        <div class="button approve"
             on:click={approve}>
            <span class="material-icons-round">thumb_up</span>
            Approve
        </div>
        <div class="button reject"
             on:click={reject}>
            <span class="material-icons-round">thumb_down</span>
            Reject
        </div>
    </div>
    <div class="priceWrap">
        <div class="price">{formatter.format(originalPrice)}</div>
        <div class="material-icons-round">east</div>
        <div class="price">{formatter.format(price)}</div>
    </div>
    <div class="store">
        {store.name}
        -
        {store.streetAddress}, {store.city}
    </div>
    <!--    <div class="dates">-->
    <!--    </div>-->
    <!--    {#each sales as sale}-->
    <!--        <div class="sale">-->
    <!--            <div>{sale.item.item.name}</div>-->
    <!--            <div class="price">&#11166; {formatter.format(sale.item.price)}&rightarrow;-->
    <!--                {formatter.format(sale.newPrice)}</div>-->
    <!--        </div>-->
    <!--    {/each}-->
</div>

<style>
    .button {
        margin: 0 0 0 0.5rem;
        display: flex;
        flex-direction: row;
        align-items: center;
    }

    .button .material-icons-round {
        margin-right: 0.3em;
    }

    .line-one {
        display: flex;
        align-items: center;
        margin-bottom: 0.5rem;
    }

    .item {
        position: relative;
        width: 100%;
        padding: 0.5rem;
        box-sizing: border-box;
        border-left: 0.2rem solid var(--accent-color);
        margin-bottom: 1rem;
    }

    .item:hover {
        background: #eee;
    }

    .item:nth-child(2n-1) {
        background: var(--bg-color-secondary);
    }

    .wrapper {
        display: block;
        height: 100%;
        margin: 0;
    }

    .price {
        background: var(--accent-color);
        color: white;
        font-size: 1.1em;
        font-weight: bold;
        border-radius: 0.2em;
        padding: 0.4em;
        display: inline-block;
        box-sizing: border-box;
        background: var(--accent-color);
    }

    .priceWrap {
        display: flex;
        align-items: center;
        padding-bottom: 0.5em;
    }

    .filler {
        flex-grow: 1;
    }

    .approve {
        background: green;
        color: white;
    }

    .reject {
        background: red;
        color: white;
    }
</style>