<script>
    import {formatter} from "$lib/util";
    import {gateway} from "$lib/variables";
    import {goto} from "$app/navigation";
    import {onMount} from "svelte";

    export let items, item, src;
    export let editable = false;
    let imgUpload;

    let gate;

    const numberRegex = /[0-9\.]/;
    const priceRegex = /^[0-9]*\.?[0-9]{0,2}$/;

    const dateOpts = {weekday: 'long', year: 'numeric', month: 'short', day: 'numeric'};

    const changeImg = e => {
        let reader = new FileReader();
        reader.onload = e => {
            src = e.target.result;
            console.log(src);
            for (let itm of items) {
                itm.item.image = src;
            }
        };
        reader.readAsDataURL(e.target.files[0])
    };

    let prices = [];
    let description, priceObj, focused, leadPrice;

    onMount(() => gate = gateway());

    const save = () => {
        console.log(item);
        for (let itm of items) {
            itm.item.description = item.item.description;
            console.log(itm.price);
            if (itm.id == item.id)
                itm.price = parseFloat(item.price);
            else
                itm.price = parseFloat(itm.price);
            console.log(itm);
        }
        console.log(items);

        fetch(`${gate}/item-service/item-details/multiple`,
            {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(items)
            }
        ).then(res => {
            console.log(res.status);
            if (res.status == 200 || res.status == 204) {
                goto("/items/" + item.item.upc, {replaceState: true});
            } else alert("Something went wrong. The item was not updated.");
        });

    };

    const numberValidate = e => {
        let text = e.target.innerText;
        let caretPos = 0;
        if (window)
            caretPos = window.getSelection().getRangeAt(0).startOffset;
        let newText = text;
        if (e.key.length == 1) {
            let t1 = text.substring(0, caretPos) + e.key;
            let t2 = text.substring(caretPos);
            newText = t1 + t2;
        }

        if (!numberRegex.test(e.key) || !priceRegex.test(newText)) {
            e.stopPropagation();
            e.preventDefault();
        }
    };

    $: console.log(item.price);
</script>

<div class="parent full">
    {#if !items}
        <div>Item not found</div>
    {:else if item}
        <div class="hcenter" id="imgBox">
            <img alt="{item.item.name}" on:click={() => {
                if(editable) imgUpload.click();
            }} src={item.item?.image ? item.item.image :
            src} class:uploadPhoto={editable}/>
            <input accept="image/*" bind:this={imgUpload} id="image" name="image" on:change={changeImg}
                   type="file"/>
            {#if editable}
                <div class="material-icons-round addPhoto" on:click={() => imgUpload.click()}>add_a_photo</div>
            {/if}
            <div class="clear"/>
            <div></div>
        </div>
        <div class="itemBody">
            {#if editable}
                <h1 contenteditable="true" bind:textContent={item.item.name}/>
            {:else}
                <h1 contenteditable="false">{item.item.name}</h1>
            {/if}
            <div class="line">
                {#if editable}
                    <div class="price editable" bind:this={priceObj}
                         class:focused={focused == priceObj}
                         on:click={() => {leadPrice.focus()}}>$
                        <div class="cost" contenteditable="true" bind:textContent={item.price}
                             bind:this={leadPrice}
                             on:keypress={numberValidate}
                             on:blur={() => focused = undefined}
                             on:focus={e => focused = e.target.parentElement}></div>
                    </div>
                {:else}
                    <div class="priceWrap">
                        <div class="price" class:strike={item.onSale} bind:this={priceObj}>
                            ${item.price.toFixed(2)}
                        </div>
                        {#if item.onSale}
                            <div class="material-icons-round">east</div>
                            <div class="price sale" bind:this={priceObj}>
                                ${item.salePrice.toFixed(2)}
                            </div>
                        {/if}
                    </div>
                {/if}

                {#if editable}
                    <div class="stock button edit" class:inStock={item.inStock} class:outOfStock={!item.inStock}
                         on:click={() => item.inStock = !item.inStock}>
                        {item.inStock ? "In Stock" : (item.carried ? "Out of Stock" : "No Longer Carried")}
                    </div>
                {:else}
                    <div class="stock" class:inStock={item.inStock} class:outOfStock={!item.inStock}>
                        {item.inStock ? "In Stock" : (item.carried ? "Out of Stock" : "No Longer Carried")}
                    </div>
                {/if}
                <strong>{item.store.name}</strong> - {item.store.streetAddress}, {item.store.city}
                {#if item.lastUpdated}
                    <div class="updated">
                        <strong>Last updated: </strong>
                        {new Date(item.lastUpdated).toLocaleDateString('en-us', dateOpts)}
                        at
                        {new Date(item.lastUpdated).toLocaleTimeString('en-US', {timeStyle: "short"})}
                    </div>
                {/if}
            </div>
            <hr/>
            {#if editable}
                <div class="text" bind:this={description} contenteditable="true"
                     bind:innerHTML={item.item.description}></div>
            {:else}
                <div class="text">{@html item.item.description}</div>
            {/if}
        </div>

        {#if editable}
            <div class="save">
                <div class="button saveBtn" on:click={save}>Save</div>
            </div>
        {/if}

        {#if items.length > 1}
            <h4 class="clear">Also available at...</h4>
            {#each items as itm, i}
                {#if itm.store != item.store}
                    <div class="other">
                        {#if editable}
                            <div class="price editable" bind:this={prices[i]}
                                 class:focused={focused == prices[i]}
                                 on:click={() => {prices[i].children[0].focus()}}>$
                                <div class="cost" contenteditable="true" bind:textContent={itm.price}
                                     on:keypress={numberValidate}
                                     on:blur={() => focused = undefined}
                                     on:focus={e => focused = e.target.parentElement}></div>
                            </div>
                            <div class="stock button edit" class:inStock={itm.inStock} class:outOfStock={!itm.inStock}
                                 on:click={() => itm.inStock = !itm.inStock}>
                                {itm.inStock ? "In Stock" : (itm.carried ? "Out of Stock" : "No Longer Carried")}
                            </div>
                        {:else}
                            <div class="price">{formatter.format(itm.price)}</div>
                            <div class="stock" class:inStock={itm.inStock} class:outOfStock={!itm.inStock}>
                                {itm.inStock ? "In Stock" : (itm.carried ? "Out of Stock" : "No Longer Carried")}
                            </div>
                        {/if}
                        <strong>{itm.store.name}</strong> - {itm.store.streetAddress}, {itm.store.city}
                    </div>
                {/if}
            {/each}
        {/if}
    {/if}
</div>

<style>
    .parent {
        margin: 10px;
    }

    .itemBody {
        /*display: flex;*/
        flex-direction: column;
    }

    img {
        width: 30vw;
        height: 30vw;
        max-width: 300px;
        max-height: 300px;
        object-fit: contain;
        box-sizing: border-box;

        border: 2px solid black;
        box-shadow: 5px 5px 5px #333;
        padding: 10px;
    }

    h1 {
        font-size: 2.3em;
        margin: 0.5rem 0;
        padding: 0.5rem;
    }

    hr {
        /*width: 100%;*/
    }

    #imgBox {
        position: relative;
        z-index: 5;
        float: left;
        /*margin-bottom: 10px;*/
        margin-right: 15px;
        margin-bottom: 10px;
        background: var(--bg-color);
    }

    input[type="file"] {
        display: none;
    }

    .priceWrap {
        display: inline-flex;
        align-items: center;
    }

    .price, .inStock, .outOfStock {
        font-size: 1.1em;
        font-weight: bold;
        border-radius: 0.2em;
        padding: 0.4em;
        display: inline-block;
        color: white;
    }

    .price {
        display: inline-flex;
        height: 100%;
        box-sizing: border-box;
        background: var(--accent-color);
    }

    .stock.edit:hover {
        cursor: pointer;
    }

    .inStock {
        background: limegreen;
    }

    .outOfStock {
        background: indianred;
    }

    .other {
        /*margin-left: 1em;*/
        /*padding-left: 0.5em;*/
        /*border-left: 0.2em solid var(--accent-color);*/
    }

    .focused {
        outline: 2px solid black;
    }

    .cost:focus {
        outline: none;
    }

    .save {
        display: flex;
        justify-content: center;
    }

    *:not(.cost)[contenteditable="true"], .editable {
        border: 2px dashed var(--disabled-fg-color);
    }

    *:not(.cost, .price)[contenteditable="true"] {
        padding: 0.5em;
    }

    .addPhoto {
        position: absolute;
        bottom: -0.3em;
        right: -0.3em;
        background: var(--bg-color);
        color: var(--fg-color);
        padding: 0.3em;
        border-radius: 50%;
        border: 1px solid var(--fg-color);
    }

    .addPhoto:hover, .uploadPhoto:hover {
        cursor: pointer;
    }

    .strike {
        text-decoration: line-through;
        /*margin-right: 0.5em;*/
    }

    .sale {
        text-decoration: underline;
        background: #f44;
    }
</style>