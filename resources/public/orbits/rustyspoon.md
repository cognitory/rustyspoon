# Rustyspoon

Demo of the final result:

@@@search@@@


## Prep

Before you get started, make sure you have [Java and Leiningen installed](http://cognitory.github.io/codex/#/guides/setup-env).

## Hello World

Create a folder `rustyspoon` for your project someplace, for example, on your Desktop.

Open the project folder with your editor.

Create files and folders so that your project folder structure looks like this:

```misc
rustyspoon
  project.clj
  resources
   public
     index.html
  src
   rustyspoon
     core.cljs
```

Edit `project.clj` to have the following content:

```clojure
(defproject rustyspoon "0.0.1"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.494"]
                 [re-frame "0.9.2"]
                 [garden "1.3.2"]]

  :plugins [[lein-figwheel "0.5.9"]]

  :figwheel {:server-port 3499}

  :cljsbuild {:builds
              [{:id "dev"
                :figwheel true
                :source-paths ["src"]
                :compiler {:main rustyspoon.core
                           :asset-path "/js/dev"
                           :output-to "resources/public/js/dev.js"
                           :output-dir "resources/public/js/dev"
                           :verbose true}}]})
```

Edit `core.cljs` to have the following content:

!!!hello world/0-4!!!

Edit `index.html` to have the following content:

```html
<!DOCTYPE html>
<html>
  <head></head>
  <body>
    <div id="app"></div>
    <script src="/js/dev.js" type="text/javascript"></script>
  </body>
</html>
```

Open a Terminal window, and go to the folder you created:

```sh
cd ~/Desktop/rustyspoon
```

Run Figwheel:

```sh
rlwrap lein figwheel
```

In Chrome, go to `http://localhost:3499`

You should see the text `Hello World!` on the page.

In Chrome, do `View > Developer > Javascript Console`, which should bring up a console, and in the logs it should say `Hello Console!`.



## Making a List of Restaurants to Display

Let's start by displaying a list of restaurants. To do that, we first need information about a few restaurants. We will create a 'vector' of 'maps', each map containing information about a restaurant.

Inside of `core.cljs` add the following below `(enable-console-print!)`:

!!!define restaurants array/0-!!!

@@@define restaurants array@@@

`def` is used to gives names to things that we will use later. Here, we define a list of restaurants using `(def restaurants ...)`

The square brackets `[ ... ]` are used to create a *vector*, which is an ordered list of things. For example, here is a vector of a few numbers: `[ 1 3 5 6 7 ]`.

Our restaurant vector is a list of maps.
A *map* is created by using brackets `{ ... }`.
A map contains multiple *keys* and *values*, with each *key* corresponding to some *value* -- you can think of it like a dictionary, where words are keys and their definitions are values, or a phonebook (names = keys, numbers = values).

We can look up a value from a map by doing `(some-map :my-key)`.
For example, if we had `(def my-map {:name "Canoe" :rating 3.9})` then `(my-map :name)` would give `"Canoe"`.
If the key we're looking up is a *keyword* -- i.e. it starts with a colon, like `:name` and `:rating` above -- then we can also use the opposite order for looking up, like `(:rating my-map)` to get `9.3`.

In our restaurant vector, each map represents a restaurant.
Each of these contains the `:name`, `:address`, `:image`, `:rating` and `:price-range` keys, with either strings or numbers as values.

Let's get these restaurants showing!

Replace your `(defn app-view ...)` with the following:

!!!update app-view to show restaurants/0-!!!

@@@update app-view to show restaurants@@@

Save the file. Your browser should immediately update and show a list of restaurants, each with a name and address.

`(defn app-view ...)` defines a *function* called `app-view`. A function is like a mini program that can take in various inputs and, manipulate those inputs, and return an output. For example, the following function will square a number passed into it: `(defn square [x] (* x x))` This function can be used in other parts of our program by writing: `(square 3)`

Our `app-view` function takes no inputs (the `[]` beside its name has nothing inside). It will return a data structure similar to:

```clojure
[:div.app
  [:div.restaurant
    [:div.name "Byblos]
    [:div.address "11 Duncan Street"]]
  [:div.restaurant
    [:div.name "George"]
    [:div.address "111 Queen St. E"]]]
```

This structure is a vector with many nested *keywords* and vectors.

The `(for [...] ...)` construct provides a way to do the same operation on a collection of data.
It has two parts: (1) what we're going to loop over, and (2) what we're going to do on each thing.

In our code, the "what we're going to loop over" is `[r restaurants]`.
This says we will be going over each element in the collection named `restaurants` and we will call the element we're currently looking at `r`.
In Clojure, we call this a *binding form*.
We could call the variable anything we want -- we could write `(for [foobar restaurants] ... (foobar :name))` if we wanted.
The variable name we use in a loop is something we choose based on what will make it clear what's happening when we read the code later.

The "what we're going to do for each thing" part, often called the *loop body* is the `[:li [:div.name (r :name)] [:div.address (r :address)]]`.
You can see that the variable `r` which we declared above in the *binding form* is being used in the body.

Reagent will take this vector and convert it to the corresponding *HTML*, which the browser understands how to display. The corresponding HTML to the above is:

```html
<div class='app'>
  <div class='restaurant'>
    <div class='name'>Byblos</div>
    <div class='address'>11 Duncan Street</div>
  </div>
  <div class='resturant'>
    <div class='name'>George</div>
    <div class='address'>111 Queen St. E</div>
  </div>
</div>
```

In the above HTML, we see many `div` elements, which are a generic container of other HTML (as opposed to something like `p` should be used for creating paragraphs of text).
The divs each have a `class` attribute, which will later help us refer to the various elements when we start to style them (the classes also help us remember what that part of the code is displaying).
HTML elements in general look like `<tag attribute="some value" other-attribute="other value">...</tag>`.
The `...` part can be more HTML, text, or nothing.

We are using a Clojure style called "Hiccup" to represent HTML using vectors.
The above example in Hiccup would look like `[:tag {:attribute "some value" :other-attribute "other value"} ...]`.
This is a more compact way of representing the HTML that also lets us use Clojure functions to manipulate the HTML we generate.

## Displaying Images

The `:image` values in our restaurant data are ids of images taken from Yelp. To display them, we need the full URL, so let's add a little helper function to get the image link for a particular restaurant. Add the following above `(def restaurants ...)`:

!!!add image function/0-!!!

@@@add image function@@@

This is defining a function called `id->image` that takes one argument called `id`.
We then use the `str` function to attach that id to the end of the url that will give us a link to the appropriate image.

Now, edit `app-view` to include:

!!!display image/0-!!!

@@@display image@@@

Now your page should be showing an image for each restaurant.

## Show Other Restaurant Info

Now, try changing the code to have the other restaurant information show up (`:rating` and `:price-range`).

.

.

.

Did you figure it out?

You can show the rating and price-range by adding the following in your `app-view`:

!!!show other restaurant info/0-!!!

!!!show other restaurant info/1-!!!

For the price-range, we used the `repeat` function to show multiple "$". Neat, huh?!

@@@show other restaurant info@@@


## Styling

Let's make things a bit prettier. Add the following above `id->image`:

!!!add styles/0-!!!

And add the following to `app-view`:

!!!add styles/1-!!!

@@@add styles@@@

Now our images should be a reasonable size 'floating' to the left of the restaurant info. The restaurant names are bold and the price ranges are green.

## Factor out `restaurant-view`:

Next, lets move all the code related to a single restaurant into its own view.

Create a `restaurant-view` before `app-view` that looks like the following (you can cut/paste most of it from `app-view`):

!!!factor out a restaurant-view/0-!!!

Replace what you removed from `app-view` with:

!!!factor out a restaurant-view/1-!!!

@@@factor out a restaurant-view@@@

Our `app-view` now loops over the restaurants and passes each restaurant `r` to the `restaurant-view`.


## Creating Our Header w/ Search Field and Filter Buttons

Now let's start working on the header that will contain the search field and the buttons to sort and filter the list of restaurants.

First, we'll create a header that doesn't do anything, and we'll gradually make the buttons and text field work.

Add a `header-view` before `restaurant-view`:

!!!add a header-view/0-!!!

Add the `header-view` to `app-view`:

!!!add a header-view/1-!!!

Also, let's style the header. Update `styles` to include some `.header` styles:

!!!add a header-view/2-!!!

@@@add a header-view@@@

Now you should see a red header above the list of restaurants.


## Implementing Sort Toggle Buttons

Our `header-view` contains two buttons that we will use to change how our list is sorted: "Name" and "Rating". Before we hook them up, let's just sort our existing list.

For sorting, we can use the `sort-by` function, which takes in a list and a function or key to sort with. In our case, we can sort by rating by doing: `(sort-by :rating restaurants)` instead of just `restaurants`.

Update `app-view` to the following:

!!!sorting our list/0-!!!

@@@sorting our list@@@

After you hit save, the list of restaurants should be sorted by rating.

Try changing the sort-key from `:rating` to `:name`. The order of the restaurants on your page should change.

### Reversing the Sort

Hmmm... normally we'd want the list to be supported by rating from highest to lowest, but it's lowest to highest right now. Let's reverse the list:

Add a `(reverse ...)` around our `sort-by`. Your `app-view` should now be:

!!!reverse the sort/0-!!!

@@@reverse the sort@@@

### App State

Now that we have a way to sort the list, we need a way to change how it is sorted. It'd be great if we could just change the key we use in the `sort-by` (to be either `:rating` or `:name` depending on which button was pressed).

Up until now, our app has just shown whatever we've written, but it did not change once loaded (the app reloading when you save code doesn't count, because it's essentially loading your code from scratch). In other words, your app has been in a single 'state', whatever state we'd coded it to be in. What we'd like, however, is for our app to be able to be in different states.

To be able to sort the list in two different ways, we want our app to be in one of two states: (1) sorted by `:name`, or (2) sorted by `:rating`. We can represent the state of our app with a map: for now, the state can either be: `{:sort :rating}` or `{:sort :name}`.

We are going to define an `app-state` 'atom', which will store a map of whatever information we need to represent our app's states. What's an atom? For now, all you need to know is that atoms are what we use in Clojure to create data structures that can be changed over time (in other programming languages, you would use a 'variable').

Add the following above `styles`:

!!!implementing sort toggle/0-!!!

This defines `app-state` to be an atom whose initial value is `{:sort :rating}` (a map).

Now lets change our `app-view` to use the value from the atom instead of having it preset. In your `app-view`, replace `:rating` with `(@app-state :sort)`. `app-view` should now look like the following:

!!!implementing sort toggle/1-!!!

`@app-state` is how we get access to the data in `app-state`. Since it's a map, we can get the value of the `:sort` key by doing `(@app-state :sort)`.


### Wiring Up the Sort Buttons

Now let's make the buttons actually do something.

First, create a function to change the value of `:sort` in `app-state`. Add the following below `app-state`:

!!!implementing sort toggle/1-!!!

Next, we will make our buttons use the `set-sort!` function we just defined. Swap out the existing `[:div.sort ...]` in your `header-view` with:

!!!implementing sort toggle/3-!!!

@@@implementing sort toggle@@@

Try it out. The buttons should change how the list is sorted now.

### Changing the look of the sort buttons

It'd be nice if the buttons changed visually depending on which sort was currently applied. We can use the state of the `:sort` in `app-state` to decide how to style each button. If the `:sort` matches the button, we will assign a class `active` to the button, and apply styles to it.

Modify the option maps for your `:buttons` in your `header-view` so that you get the following:

!!!styling sort buttons/0-!!!

We also need to define some styles for the active class. Add the following inside of your `.header` styles:

!!!styling sort buttons/1-!!!

@@@styling sort buttons@@@

When you save, one of the sort buttons should be red and the other grey. When you click on the grey button, it should turn red, the other should turn grey, and the sort should change.

### Prettier Buttons

While we're here, let's improve the look of the buttons.

Add the following inside of your `.header` styles:

!!!styling buttons better/0-1!!!

Replace your `.button` styles with:

!!!styling buttons better/2-!!!

@@@styling buttons better@@@

## Price Range Filtering

Now let's make the price range buttons filter the results.

The approach will be very similar to making the sort buttons work. We will need to have:

1. a key in `app-state` to keep track of which price range buttons are active (the "state" of the price range filters),

2. a function to change the `restaurants` list before we loop over it

3. a function to update `app-state`

4. `:on-click` functions to call the function from (3)

5. logic to set a `:class` of "active" on the relevant buttons

6. styles for active buttons

Try implementing as much as you can on your own.

.

.

.

Need some help? Here are a few hints (they correspond to each requirement above):

1. you can use a `:price-ranges` key in `app-state` with a "set" to keep track of the active sorts (ex. `#{1 2 3 4}`)

2. you can use `filter` to adjust the `restaurants` list

3. create a `toggle-price-range` function (you can use `conj` and `disj` to update the values stored in a set)

4. you just need to use `toggle-price-range` from (3)

5. you can use `contains?` to determine if a button should be styled as active

6. you shouldn't need to change the styles at all

.

.

.

How'd it go? Here's how we did it (it's okay if your code looks different):


### 1. a new key in `app-state`

!!!price range filtering/0-!!!

### 2. a function to filter `restaurants`

!!!price range filtering/1-!!!

### 3. a function to update `app-state`

!!!price range filtering/2-!!!

### 4. `:on-click` functions

!!!price range filtering/3-!!!

### 5. setting buttons as active

!!!price range filtering/4-!!!

@@@price range filtering@@@


### Refactor Price Range Filters with a Loop

!!!refactor price range buttons/0-!!!

@@@refactor price range buttons@@@


# Implementing Search

Make typing in search field change `:query` in `app-state`.


Add `:query` key to `app-state`:

!!!search/0-!!!

Add a new function to change the `:query` in `app-state`:

!!!search/1-!!!

Make the search field run the `set-query` function every time the text inside changes:

!!!search/2-!!!

Filter the restaurants in `app-view` by the query in `app-state`:

!!!search/3-!!!

@@@search@@@

# That's It For Now

Coming soon:

- Refactor to use Transactions
- Refactor to use Subscriptions
- Move CSS Into a Seperate File
- Make Selecting a Restaraunt Show Its Page
- Allow Creating of a New Restaurant
- Pull Restaurants from a Database
- Add New Restaurants to a Database
- Users Can Leave Comments (+ save to database)
- Users Can Register and Login
- Users Can Rate Restaurants
- Users Have Profiles
- Different URLs for different pages
